package br.cin.ufpe.dass.ontologycatalog.services;

import br.cin.ufpe.dass.ontologycatalog.model.*;
import br.cin.ufpe.dass.ontologycatalog.repository.ClassNodeRepository;
import br.cin.ufpe.dass.ontologycatalog.repository.DataPropertyRepository;
import br.cin.ufpe.dass.ontologycatalog.repository.ObjectPropertyRepository;
import br.cin.ufpe.dass.ontologycatalog.repository.OntologyNodeRepository;
import br.cin.ufpe.dass.ontologycatalog.services.exception.OntologyCatalogException;
import edu.smu.tspell.wordnet.WordNetDatabase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/***
 * @author diego
 *
 * Services regarding the management of ontologies imported to the catalog
 */
@Service
public class OntologyCatalogService {

    private  final ClassNodeRepository classNodeRepository;

    private final OntologyNodeRepository ontologyNodeRepository;

    private final ObjectPropertyRepository objectPropertyRepository;

    private final DataPropertyRepository dataPropertyRepository;

    private OWLReasoner reasoner;

    private final WordNetDatabase wordNetDatabase;

    public OntologyCatalogService(ClassNodeRepository classNodeRepository, OntologyNodeRepository ontologyNodeRepository, ObjectPropertyRepository objectPropertyRepository, DataPropertyRepository dataPropertyRepository, WordNetDatabase wordNetDatabase) {
        this.classNodeRepository = classNodeRepository;
        this.ontologyNodeRepository = ontologyNodeRepository;
        this.objectPropertyRepository = objectPropertyRepository;
        this.dataPropertyRepository = dataPropertyRepository;
        this.wordNetDatabase = wordNetDatabase;
    }

    public DataPropertyNode getOrCreateDataPropertyNodeWithUniqueFactory(DataPropertyNode dataPropertyNode) {
        return dataPropertyRepository.findById(dataPropertyNode.getName()).orElse(dataPropertyRepository.save(dataPropertyNode));
    }

    public ObjectPropertyNode getOrCreateObjectPropertyNodeWithUniqueFactory(ObjectPropertyNode objectPropertyNode) {
        return objectPropertyRepository.findById(objectPropertyNode.getName()).orElse(objectPropertyRepository.save(objectPropertyNode));
    }

    public ClassNode getOrCreateClassNodeWithUniqueFactory(ClassNode classNode) {
        return classNodeRepository.findById(classNode.getName()).orElse(classNodeRepository.save(classNode));
    }

    public OntologyNode getOrCreateOntologyNodeWithUniqueFactory(OntologyNode ontologyNode) {
        return ontologyNodeRepository.findById(ontologyNode.getName()).orElse(ontologyNodeRepository.save(ontologyNode));
    }
    public void importOntologyAsGraph(IRI iri) throws Exception {
        OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntology(iri);
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);
        if (!reasoner.isConsistent()) {
            throw new OntologyCatalogException("Ontology is inconsistent");
        }
        reasoner.precomputeInferences();

        OntologyNode ontologyNode = new OntologyNode();
        ontologyNode.setName(iri.getShortForm());
        ontologyNode.setUri(iri.toURI().toString());
        ontologyNode.setVersion(ontology.getOntologyID().getVersionIRI().toString());
        importOntologyClasses(ontologyNode, ontology);
        importObjectProperties(ontology);
        importDataProperties(ontologyNode, ontology);
        ontologyNodeRepository.save(ontologyNode);
    }

    public void importObjectProperties(OWLOntology ontology) {
        ontology.objectPropertiesInSignature().forEach(owlObjectProperty -> {

            ObjectPropertyNode propertyNode = new ObjectPropertyNode(extractElementName(owlObjectProperty.toString()));

            reasoner.objectPropertyDomains(owlObjectProperty, true).forEach( domain -> {
                String domainClassName = extractElementName(domain.toString());
                ClassNode domainClass = classNodeRepository.findById(domainClassName).orElseThrow(() -> new RuntimeException(String.format("Class %s does not exists to be used as domain", domainClassName)));
                if (!propertyNode.getDomain().contains(domainClass)) {
                    propertyNode.getDomain().add(domainClass);
                }
            });
            reasoner.objectPropertyRanges(owlObjectProperty, true).forEach( range -> {
                String rangeClassName = extractElementName(range.toString());
                ClassNode rangeClass = classNodeRepository.findById(rangeClassName).orElseThrow(() -> new RuntimeException(String.format("Class %s does not exists to be used as range", rangeClassName)));
                if (!propertyNode.getRange().contains(rangeClass)) {
                    propertyNode.getRange().add(rangeClass);
                }
            });

            objectPropertyRepository.save(propertyNode);
        });
    }

    public void importDataProperties(OntologyNode ontologyNode, OWLOntology ontology) {

        ontology.dataPropertiesInSignature().forEach(owlDataProperty -> {

            DataPropertyNode propertyNode = new DataPropertyNode(extractElementName(owlDataProperty.toString()));

            reasoner.dataPropertyDomains(owlDataProperty, true).forEach( domain -> {
                String domainClassName = extractElementName(domain.toString());
                ClassNode domainClass = classNodeRepository.findById(domainClassName).orElseThrow(() -> new RuntimeException(String.format("Class %s does not exists to be used as domain", domainClassName)));
                if (!propertyNode.getDomain().contains(domainClass)) {
                    propertyNode.getDomain().add(domainClass);
                }
            });

            ontology.dataPropertyRangeAxioms(owlDataProperty).forEach(dataProperty -> {
                String range = extractElementName(dataProperty.getRange().toString());
                if (!propertyNode.getRange().contains(range)) {
                    propertyNode.getRange().add(range);
                }
            });

            dataPropertyRepository.save(propertyNode);
        });
    }

    public void importOntologyClasses(OntologyNode ontologyNode, OWLOntology ontology) {

        ClassNode thing = new ClassNode();
        thing.setName("owl:Thing");
        getOrCreateClassNodeWithUniqueFactory(thing);
        if (!ontologyNode.getClasses().contains(thing)) {
            ontologyNode.getClasses().add(thing);
        }

        ontology.classesInSignature().forEach(owlClass -> {
            ClassNode classNode = new ClassNode();
            classNode.setName(extractElementName(owlClass.toString()));
            Set<String> synset = Stream.of(wordNetDatabase.getSynsets(classNode.getName())).flatMap(s -> Stream.of(s.getWordForms())).distinct().collect(Collectors.toSet());;
            classNode.setSynonyms(synset.stream().map(s -> new SynonymNode(s)).collect(Collectors.toSet()));

            //Import super classes
            Supplier<Stream<OWLClass>> superClasses = new Supplier<Stream<OWLClass>>() {
                @Override
                public Stream<OWLClass> get() {
                    return reasoner.superClasses(owlClass, true);
                }
            };
            if (superClasses.get().count() == 0 && !classNode.getName().equals("owl:Thing")) {
                //Create relation to thing node
                classNode.getSuperClasses().add(thing);
            } else {
                superClasses.get().forEach(superClass -> {
                    OWLClassExpression parent = superClass.asOWLClass();
                    String parentString = extractElementName(parent.toString());
                    ClassNode parentNode = new ClassNode(parentString);
                    classNode.getSuperClasses().add(parentNode);
                });
            }

            getOrCreateClassNodeWithUniqueFactory(classNode);

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

}
