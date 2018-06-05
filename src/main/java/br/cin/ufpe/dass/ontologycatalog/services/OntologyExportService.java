package br.cin.ufpe.dass.ontologycatalog.services;

import br.cin.ufpe.dass.ontologycatalog.model.ClassNode;
import br.cin.ufpe.dass.ontologycatalog.model.DataPropertyNode;
import br.cin.ufpe.dass.ontologycatalog.model.ObjectPropertyNode;
import br.cin.ufpe.dass.ontologycatalog.repository.ClassNodeRepository;
import br.cin.ufpe.dass.ontologycatalog.repository.DataPropertyRepository;
import br.cin.ufpe.dass.ontologycatalog.repository.ObjectPropertyRepository;
import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class OntologyExportService {

    public OntologyExportService(ClassNodeRepository classNodeRepository, DataPropertyRepository dataPropertyRepository, ObjectPropertyRepository objectPropertyRepository) {
        this.classNodeRepository = classNodeRepository;
        this.dataPropertyRepository = dataPropertyRepository;
        this.objectPropertyRepository = objectPropertyRepository;
    }

    private final ClassNodeRepository classNodeRepository;

    private final DataPropertyRepository dataPropertyRepository;

    private final ObjectPropertyRepository objectPropertyRepository;

    public void exportOntologyAsFile(String ontologyName, String filePath) throws OWLOntologyCreationException, OWLOntologyStorageException {

        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = ontologyManager.getOWLDataFactory();

        String base = String.format("http://%s", ontologyName);
        PrefixManager pm = new DefaultPrefixManager(null, null, base);

        OWLOntology ontology = ontologyManager.createOntology(IRI.create(base));

        //list classes and isA relationships
        String parametizedOntologyName = String.format(".*%s.*", ontologyName);

        Stream<ClassNode> ontologyClasses = classNodeRepository.listAllByOntologyName(parametizedOntologyName);
        ontologyClasses.forEach(classNode -> {
            OWLClass newClass = dataFactory.getOWLClass(String.format("%s", classNode.getName()));
            for (ClassNode superClass : classNode.getSuperClasses()) {
                OWLClass owlSuperClss = dataFactory.getOWLClass(String.format("%s", superClass.getName()));
                ontologyManager.addAxiom(ontology, dataFactory.getOWLSubClassOfAxiom(newClass, owlSuperClss));
            }
        });

        //list data properties
        Stream<DataPropertyNode> dataPropertyNodeStream = dataPropertyRepository.listAllByOntologyName(parametizedOntologyName);
        dataPropertyNodeStream.forEach(dataPropertyNode -> {
            OWLDataProperty newProperty = dataFactory.getOWLDataProperty(String.format("%s", dataPropertyNode.getName()));
            dataPropertyNode.getDomain().forEach(domainClass -> {
                OWLClass owlDomainClass =  dataFactory.getOWLClass(String.format("%s", domainClass.getName()));
                ontologyManager.addAxiom(ontology, dataFactory.getOWLDataPropertyDomainAxiom(newProperty, owlDomainClass));
            });
            dataPropertyNode.getRange().forEach(range -> {
                OWLDataRange owlDataRange = dataFactory.getOWLDatatype(range);
                ontologyManager.addAxiom(ontology, dataFactory.getOWLDataPropertyRangeAxiom(newProperty, owlDataRange));
            });
        });

        //list object properties
        Stream<ObjectPropertyNode> objectPropertyNodeStream = objectPropertyRepository.listAllByOntologyName(parametizedOntologyName);
        objectPropertyNodeStream.forEach(objectPropertyNode -> {
            OWLObjectProperty newProperty = dataFactory.getOWLObjectProperty(String.format("%s", objectPropertyNode.getName()));
            objectPropertyNode.getDomain().forEach(domainClass -> {
                OWLClass owlDomainClass = dataFactory.getOWLClass(String.format("%s", domainClass.getName()));
                ontologyManager.addAxiom(ontology, dataFactory.getOWLObjectPropertyDomainAxiom(newProperty, owlDomainClass));
            });
            objectPropertyNode.getRange().forEach(rangeClass -> {
                OWLClass owlRangeClass = dataFactory.getOWLClass(String.format("%s", rangeClass.getName()));
                ontologyManager.addAxiom(ontology, dataFactory.getOWLObjectPropertyRangeAxiom(newProperty, owlRangeClass));
            });
        });

        File file = new File(filePath);
        ontologyManager.saveOntology(ontology, IRI.create(file.toURI()));

    }


    public void exportOntologyAsFileFromSegment(String ontologyName, List<Map<String, Object>> segmentElements, String filePath) throws OWLOntologyCreationException, OWLOntologyStorageException {

        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = ontologyManager.getOWLDataFactory();

        String base = String.format("http://%s#", ontologyName.replace("."+ FilenameUtils.getExtension(ontologyName), ""));
        PrefixManager pm = new DefaultPrefixManager(null, null, base);

        OWLOntology ontology = ontologyManager.createOntology(IRI.create(base));

        segmentElements.forEach(segment-> {

            String relationship = (String) segment.get("TYPE(r)");
            if (relationship.equals("isA")) {
                ClassNode classNode = (ClassNode) segment.get("c1");
                ClassNode superClassNode = (ClassNode) segment.get("c2");

                OWLClass newClass = dataFactory.getOWLClass(String.format("#%s", classNode.getName()));
                OWLClass owlSuperClss = dataFactory.getOWLClass(String.format("#%s", superClassNode.getName()));
                ontologyManager.addAxiom(ontology, dataFactory.getOWLSubClassOfAxiom(newClass, owlSuperClss));

            } else if (relationship.equals("domain")) {

                if (segment.get("c1") instanceof ObjectPropertyNode) {

                    ClassNode domainClass = (ClassNode)segment.get("c2");

                    OWLObjectProperty newProperty = dataFactory.getOWLObjectProperty(String.format("#%s", ((ObjectPropertyNode) segment.get("c1")).getName()));

                    OWLClass owlDomainClass = dataFactory.getOWLClass(String.format("#%s", domainClass.getName()));
                    ontologyManager.addAxiom(ontology, dataFactory.getOWLObjectPropertyDomainAxiom(newProperty, owlDomainClass));

                } else if (segment.get("c1") instanceof DataPropertyNode) {

                    ClassNode domainClass = (ClassNode)segment.get("c2");

                    OWLDataProperty newProperty = dataFactory.getOWLDataProperty(String.format("#%s", ((DataPropertyNode) segment.get("c1")).getName()));

                    OWLClass owlDomainClass = dataFactory.getOWLClass(String.format("#%s", domainClass.getName()));
                    ontologyManager.addAxiom(ontology, dataFactory.getOWLDataPropertyDomainAxiom(newProperty, owlDomainClass));

                }

            } else if (relationship.equals("range")) {

                //range as relationship is valid only for Object properties as data properties use literal types as range
                ObjectPropertyNode objectPropertyNode = (ObjectPropertyNode) segment.get("c1");
                ClassNode rangeClass = (ClassNode) segment.get("c2");

                OWLObjectProperty newProperty = dataFactory.getOWLObjectProperty(String.format("#%s", objectPropertyNode.getName()));
                OWLClass owlRangeClass = dataFactory.getOWLClass(String.format("#%s", rangeClass.getName()));
                ontologyManager.addAxiom(ontology, dataFactory.getOWLObjectPropertyRangeAxiom(newProperty, owlRangeClass));

            }


        });

        File file = new File(filePath);
        ontologyManager.saveOntology(ontology, IRI.create(file.toURI()));
    }
}
