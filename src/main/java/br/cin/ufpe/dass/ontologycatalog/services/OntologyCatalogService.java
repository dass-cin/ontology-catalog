package br.cin.ufpe.dass.ontologycatalog.services;

import br.cin.ufpe.dass.ontologycatalog.model.ClassNode;
import br.cin.ufpe.dass.ontologycatalog.model.DataPropertyNode;
import br.cin.ufpe.dass.ontologycatalog.model.ObjectPropertyNode;
import br.cin.ufpe.dass.ontologycatalog.model.SynonymNode;
import br.cin.ufpe.dass.ontologycatalog.repository.ClassNodeRepository;
import br.cin.ufpe.dass.ontologycatalog.repository.DataPropertyRepository;
import br.cin.ufpe.dass.ontologycatalog.repository.ObjectPropertyRepository;
import br.cin.ufpe.dass.ontologycatalog.services.exception.OntologyCatalogException;
import edu.smu.tspell.wordnet.WordNetDatabase;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/***
 * @author diego
 *
 * Services regarding the management of ontologies imported to the catalog
 */
@Service
public class OntologyCatalogService {

    private static String thingURI = "http://www.w3.org/2002/07/owl";

    private  final ClassNodeRepository classNodeRepository;

    private final ObjectPropertyRepository objectPropertyRepository;

    private final DataPropertyRepository dataPropertyRepository;

    private OWLReasoner reasoner;

    private final WordNetDatabase wordNetDatabase;

    private OWLDataFactory owlDataFactory;

    public OntologyCatalogService(ClassNodeRepository classNodeRepository, ObjectPropertyRepository objectPropertyRepository, DataPropertyRepository dataPropertyRepository, WordNetDatabase wordNetDatabase) {
        this.classNodeRepository = classNodeRepository;
        this.objectPropertyRepository = objectPropertyRepository;
        this.dataPropertyRepository = dataPropertyRepository;
        this.wordNetDatabase = wordNetDatabase;
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

    public void importOntologyAsGraph(IRI iri) throws OntologyCatalogException, OWLOntologyCreationException {
        OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
        owlDataFactory = owlManager.getOWLDataFactory();
        OWLOntology ontology = owlManager.loadOntology(iri);
        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);
        if (!reasoner.isConsistent()) {
            throw new OntologyCatalogException("Ontology is inconsistent");
        }
        reasoner.precomputeInferences();
        importOntologyClasses(iri, ontology);
        importObjectProperties(ontology);
        importDataProperties(ontology);
    }

    public void importOntologyClasses(IRI iri, OWLOntology ontology) {

        ontology.classesInSignature().forEach(owlClass -> {
            if (!owlClass.isAnonymous()) {

                ClassNode classNode = new ClassNode();
                classNode.setName(extractElementName(owlClass.toString()));
                classNode.setUri(owlClass.getIRI().toURI().toString());
                Set<String> synset = Stream.of(wordNetDatabase.getSynsets(classNode.getName())).flatMap(s -> Stream.of(s.getWordForms())).distinct().collect(toSet());
                ;
                classNode.setSynonyms(synset.stream().map(s -> new SynonymNode(s)).collect(toSet()));

                //Import super classes
                Supplier<Stream<OWLClass>> superClasses = () -> reasoner.superClasses(owlClass, true);
                superClasses.get().forEach(superClass -> {
                    superClass.classesInSignature().forEach(classInSignature -> {
                        if (!classInSignature.isAnonymous()) {
                            OWLClassExpression parent = classInSignature.asOWLClass();
                            String parentString = extractElementName(parent.toString());
                            ClassNode parentNode = new ClassNode(parentString);
                            parentNode.setUri(classInSignature.getIRI().toURI().toString());
                            classNode.getSuperClasses().add(parentNode);
                        }
                    });
                });

                getOrCreateClassNodeWithUniqueFactory(classNode);

            }

        });

    }

    public void importObjectProperties(OWLOntology ontology) {
        ontology.objectPropertiesInSignature().forEach(owlObjectProperty -> {

            ObjectPropertyNode propertyNode = objectPropertyRepository.findById(owlObjectProperty.getIRI().toURI().toString()).orElse(new ObjectPropertyNode(owlObjectProperty.getIRI().toURI().toString(), extractElementName(owlObjectProperty.toString())));

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

        ontology.dataPropertiesInSignature().forEach(owlDataProperty -> {

            if (owlDataProperty == null) {
                return;
            }

            String uri = owlDataProperty.getIRI().toURI().toString();
            DataPropertyNode propertyNode = dataPropertyRepository.findById(uri).orElse(new DataPropertyNode(uri, extractElementName(owlDataProperty.toString())));

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

    public Iterable<Map<String, ClassNode>> listSuperClassesByOntologyName(String ontologyName, String className) {
        String parametizedOntologyName = String.format(".*%s.*", ontologyName);
        return classNodeRepository.listSuperClassesByOntologyName(parametizedOntologyName, className);
    }

    public Iterable<Map<String, ClassNode>> listSuperClassesByOntologyName(String ontologyName) {
        String parametizedOntologyName = String.format(".*%s.*", ontologyName);
        return classNodeRepository.listSuperClassesByOntologyName(parametizedOntologyName);
    }
}
