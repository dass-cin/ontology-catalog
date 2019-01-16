/***
 * @author Diego E. R. Pessoa (derp@cin.ufpe.br)
 * Copyright (C) 2018

 * Services regarding the management of ontologies imported to the catalog
 */

package br.cin.ufpe.dass.ontologycatalog.services;

import br.cin.ufpe.dass.ontologycatalog.components.TextNormalizer;
import br.cin.ufpe.dass.ontologycatalog.model.*;
import br.cin.ufpe.dass.ontologycatalog.repository.ClassNodeRepository;
import br.cin.ufpe.dass.ontologycatalog.repository.DataPropertyRepository;
import br.cin.ufpe.dass.ontologycatalog.repository.ObjectPropertyRepository;
import br.cin.ufpe.dass.ontologycatalog.services.exception.OntologyAlreadyImported;
import br.cin.ufpe.dass.ontologycatalog.services.exception.OntologyCatalogException;
import edu.smu.tspell.wordnet.WordNetDatabase;
import org.apache.commons.io.FilenameUtils;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;


@Service
public class OntologyCatalogService {

    private Logger log = LoggerFactory.getLogger(OntologyCatalogService.class);

    private static String thingURI = "http://www.w3.org/2002/07/owl";

    private  final ClassNodeRepository classNodeRepository;

    private final ObjectPropertyRepository objectPropertyRepository;

    private final DataPropertyRepository dataPropertyRepository;

    private OWLReasoner reasoner;

    private final WordNetDatabase wordNetDatabase;

    private OWLDataFactory owlDataFactory;

    private final TextNormalizer textNormalizer;

    public OntologyCatalogService(ClassNodeRepository classNodeRepository, ObjectPropertyRepository objectPropertyRepository, DataPropertyRepository dataPropertyRepository, WordNetDatabase wordNetDatabase, TextNormalizer textNormalizer) {
        this.classNodeRepository = classNodeRepository;
        this.objectPropertyRepository = objectPropertyRepository;
        this.dataPropertyRepository = dataPropertyRepository;
        this.wordNetDatabase = wordNetDatabase;
        this.textNormalizer = textNormalizer;
    }

    public Set<String> getOntologyUris() {
        return classNodeRepository.getDistinctURIs().filter(u -> !u.equals(thingURI)).collect(toSet());
    }

    public Set<String> getOntologyNames() {
        return classNodeRepository.getDistinctURIs().filter(u -> !u.equals(thingURI)).map(u -> u.replace("http://","")).collect(toSet());
    }

    public ClassNode getOrCreateClassNodeWithUniqueFactory(ClassNode classNode) {
        return classNodeRepository.findById(classNode.getUri()).orElse(classNodeRepository.save(classNode));
    }

    public void importOntologyAsGraph(IRI iri) throws OntologyCatalogException, OWLOntologyCreationException, OntologyAlreadyImported {

        String newOntologyUri = FilenameUtils.getName(iri.toURI().toString()).replace("."+FilenameUtils.getExtension(iri.toURI().toString()), "");

        log.info("Importing {} ontology as graph ", iri);
        OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
        owlDataFactory = owlManager.getOWLDataFactory();
        OWLOntology ontology = owlManager.loadOntology(iri);
        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);
        if (!reasoner.isConsistent()) {
            throw new OntologyCatalogException("Ontology is inconsistent");
        }
        reasoner.precomputeInferences();
        importOntologyClasses(ontology);
        importObjectProperties(ontology);
        importDataProperties(ontology);
    }

    public void importOntologyClasses(OWLOntology ontology) {
        log.info("Importing ontology classes");
        ontology.classesInSignature().forEach(owlClass -> {
            if (!owlClass.isAnonymous()) {

                ClassNode classNode = new ClassNode();
                classNode.setName(extractElementName(owlClass.toString()));
                classNode.setUri(owlClass.getIRI().toURI().toString());
                String[] pieces = classNode.getName().split("(?=\\p{Upper})");
                Set<String> synset = new HashSet<>();
                String className = classNode.getName();
                if (pieces.length > 1) {
                    className = Arrays.stream(pieces).reduce((x,y) -> x + " " + y).orElse(classNode.getName());
                }
                synset.addAll(Stream.of(wordNetDatabase.getSynsets(className)).flatMap(s -> Stream.of(s.getWordForms())).distinct().filter(s -> !s.equals(classNode.getName())).collect(toSet()));

                classNode.setKeywords(synset.stream().map(s -> new KeywordNode(s)).collect(toSet()));
                classNode.getKeywords().add(new KeywordNode(textNormalizer.advancedNormalizing(classNode.getName())));

                //Import super classes
                Supplier<Stream<OWLClass>> superClasses = () -> reasoner.superClasses(owlClass, true);
                superClasses.get().forEach(superClass -> {
                    superClass.classesInSignature().forEach(classInSignature -> {
                        if (!classInSignature.isAnonymous()) {
                            OWLClassExpression parent = classInSignature.asOWLClass();
                            String parentString = extractElementName(parent.toString());
                            ClassNode parentNode = new ClassNode(parentString);
                            String[] parentPieces = parentNode.getName().split("(?=\\p{Upper})");
                            Set<String> parentSynset = new HashSet<>();
                            String parentClassName = parentNode.getName();
                            if (pieces.length > 1) {
                                parentClassName = Arrays.stream(pieces).reduce((x,y) -> x + " " + y).orElse(parentNode.getName());
                            }
                            parentSynset.addAll(Stream.of(wordNetDatabase.getSynsets(parentClassName)).flatMap(s -> Stream.of(s.getWordForms())).distinct().filter(s -> !s.equals(classNode.getName())).collect(toSet()));
                            parentNode.setKeywords(parentSynset.stream().map(s -> new KeywordNode(s)).collect(toSet()));
                            parentNode.setUri(classInSignature.getIRI().toURI().toString());
                            classNode.getSuperClasses().add(parentNode);
                            classNode.getKeywords().add(new KeywordNode(textNormalizer.advancedNormalizing(parentNode.getName())));
                        }
                    });
                });

                getOrCreateClassNodeWithUniqueFactory(classNode);

            }

        });

    }

    public void importObjectProperties(OWLOntology ontology) {
        log.info("Importing ontology object properties");
        ontology.objectPropertiesInSignature().forEach(owlObjectProperty -> {

            ObjectPropertyNode propertyNode = objectPropertyRepository.findById(owlObjectProperty.getIRI().toURI().toString()).orElse(new ObjectPropertyNode(owlObjectProperty.getIRI().toURI().toString(), extractElementName(owlObjectProperty.toString())));
            Set<String> synset = Stream.of(wordNetDatabase.getSynsets(propertyNode.getName())).flatMap(s -> Stream.of(s.getWordForms())).distinct().filter(s -> !s.equals(propertyNode.getName())).collect(toSet());
            propertyNode.setKeywords(synset.stream().map(s -> new KeywordNode(s)).collect(toSet()));
            propertyNode.getKeywords().add(new KeywordNode(textNormalizer.advancedNormalizing(propertyNode.getName())));

            reasoner.objectPropertyDomains(owlObjectProperty, true).forEach( domainExpression -> {
                //may be a unionOf (multiple)
                domainExpression.classesInSignature().forEach(domain -> {
                    String domainClassName = extractElementName(domain.toString());
                    ClassNode domainClass = classNodeRepository.findById(domain.getIRI().toURI().toString()).orElseThrow(() -> new RuntimeException(String.format("Class %s does not exists to be used as domain", domainClassName)));
                    if (!propertyNode.getDomain().contains(domainClass)) {
                        propertyNode.getDomain().add(domainClass);
                    }
                });
            });
            reasoner.objectPropertyRanges(owlObjectProperty, true).forEach( rangeExpression -> {
                rangeExpression.classesInSignature().forEach(range -> {
                    String rangeClassName = extractElementName(range.toString());
                    ClassNode rangeClass = classNodeRepository.findById(range.getIRI().toURI().toString()).orElseThrow(() -> new RuntimeException(String.format("Class %s does not exists to be used as range", rangeClassName)));
                    if (!propertyNode.getRange().contains(rangeClass)) {
                        propertyNode.getRange().add(rangeClass);
                    }
                });
            });

            objectPropertyRepository.save(propertyNode);
        });
    }

    public void importDataProperties(OWLOntology ontology) {
        log.info("Importing ontology data properties");

        ontology.dataPropertiesInSignature().forEach(owlDataProperty -> {

            if (owlDataProperty == null) {
                return;
            }

            String uri = owlDataProperty.getIRI().toURI().toString();
            DataPropertyNode propertyNode = dataPropertyRepository.findById(uri).orElse(new DataPropertyNode(uri, extractElementName(owlDataProperty.toString())));
            Set<String> synset = Stream.of(wordNetDatabase.getSynsets(propertyNode.getName())).flatMap(s -> Stream.of(s.getWordForms())).distinct().filter(s -> !s.equals(propertyNode.getName())).collect(toSet());
            propertyNode.setKeywords(synset.stream().map(s -> new KeywordNode(s)).collect(toSet()));
            propertyNode.getKeywords().add(new KeywordNode(textNormalizer.advancedNormalizing(propertyNode.getName())));

            ontology.dataPropertyDomainAxioms(owlDataProperty).forEach( dataPropertyDomainAxiom -> {
                dataPropertyDomainAxiom.getDomain().classesInSignature().forEach(domain -> {
                    String domainClassName = extractElementName(domain.toString());
                    ClassNode domainClass = classNodeRepository.findById(domain.getIRI().toURI().toString()).orElseThrow(() -> new RuntimeException(String.format("Class %s does not exists to be used as domain", domainClassName)));
                    if (!propertyNode.getDomain().contains(domainClass)) {
                        propertyNode.getDomain().add(domainClass);
                    }
                });
            });

            ontology.dataPropertyRangeAxioms(owlDataProperty).forEach( owlDataPropertyRangeAxiom -> {
                owlDataPropertyRangeAxiom.getRange().datatypesInSignature().forEach(type -> {
                    String range;
                    if (type.isBuiltIn()) {
                        range = extractElementName(type.getBuiltInDatatype().getPrefixedName());
                    } else {
                        range = extractElementName(type.toString());
                    }
                    if (!propertyNode.getRange().contains(range)) {
                        propertyNode.getRange().add(range);
                    }
                });
            });

            dataPropertyRepository.save(propertyNode);
        });
    }



    public String extractElementName(String fullname) {
        if (fullname.contains("#")) {
            fullname = fullname.substring(
                    fullname.indexOf('#')+1,
                    fullname.lastIndexOf('>'));
        }
        return fullname;
    }

    public List<ClassNode> listClassesByOntologyName(String ontologyName) {
        String parametizedOntologyName = String.format(".*%s.*", ontologyName);
        return classNodeRepository.listAllByOntologyName(parametizedOntologyName).collect(toList());
    }

    public List<DataPropertyNode> listDataPropertiesByOntologyName(String ontologyName) {
        String parametizedDataProperty = String.format(".*%s.*", ontologyName);
        return dataPropertyRepository.listAllByOntologyName(parametizedDataProperty).collect(toList());
    }

    public List<ObjectPropertyNode> listObjectPropertiesByOntologyName(String ontologyName) {
        String parametizedDataProperty = String.format(".*%s.*", ontologyName);
        return objectPropertyRepository.listAllByOntologyName(parametizedDataProperty).collect(toList());
    }

    public List<Map<String, Object>> getQueryResult(String cypherQuery) {
        return classNodeRepository.getQueryResult(cypherQuery).collect(Collectors.toList());
    }

}
