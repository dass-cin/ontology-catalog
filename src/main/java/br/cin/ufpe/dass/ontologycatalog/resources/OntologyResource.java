package br.cin.ufpe.dass.ontologycatalog.resources;

import br.cin.ufpe.dass.ontologycatalog.model.ClassNode;
import br.cin.ufpe.dass.ontologycatalog.model.DataPropertyNode;
import br.cin.ufpe.dass.ontologycatalog.model.ObjectPropertyNode;
import br.cin.ufpe.dass.ontologycatalog.services.OntologyCatalogService;
import br.cin.ufpe.dass.ontologycatalog.services.exception.OntologyCatalogException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/")
public class OntologyResource {

    private static final Logger log = LoggerFactory.getLogger(OntologyResource.class);

    private final OntologyCatalogService ontologyCatalogService;

    public OntologyResource(OntologyCatalogService ontologyCatalogService) {
        this.ontologyCatalogService = ontologyCatalogService;
    }

    @PostMapping("ontologies/import")
    public ResponseEntity<?> importOntologyFromuri(@RequestBody String uri) {
        ResponseEntity<?> response = null;
        //uri: "file:///Users/diego/ontologies/conference/cmt.owl"
        IRI sourceIri = IRI.create(URI.create(uri));
        try {
            ontologyCatalogService.importOntologyAsGraph(sourceIri);
            response = ResponseEntity.ok().body(null);
        } catch (OntologyCatalogException | OWLOntologyCreationException e ) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("errorMessage", e.getMessage());
            response = ResponseEntity.badRequest().headers(headers).body(null);
        }
        return response;
    }

    @GetMapping("ontologies/")
    public ResponseEntity<?> getOntologies() {
        return ResponseEntity.ok().body(ontologyCatalogService.getOntologyNames());
    }

    @GetMapping("ontologies/classes/{ontology}")
    public ResponseEntity<List<ClassNode>> getOntologyClasses(@PathVariable("ontology") String ontologyName) {
        return ResponseEntity.ok().body(ontologyCatalogService.listClassesByOntologyName(ontologyName));
    }

    @GetMapping("ontologies/data-properties/{ontology}")
    public ResponseEntity<List<DataPropertyNode>> getDataProperties(@PathVariable("ontology") String ontologyName) {
        return ResponseEntity.ok().body(ontologyCatalogService.listDataPropertiesByOntologyName(ontologyName));
    }

    @GetMapping("ontologies/object-properties/{ontology}")
    public ResponseEntity<List<ObjectPropertyNode>> getObjectProperties(@PathVariable("ontology") String ontologyName) {
        return ResponseEntity.ok().body(ontologyCatalogService.listObjectPropertiesByOntologyName(ontologyName));
    }

}
