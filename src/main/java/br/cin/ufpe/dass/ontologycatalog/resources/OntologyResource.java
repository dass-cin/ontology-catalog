package br.cin.ufpe.dass.ontologycatalog.resources;

import br.cin.ufpe.dass.ontologycatalog.model.ClassNode;
import br.cin.ufpe.dass.ontologycatalog.model.DataPropertyNode;
import br.cin.ufpe.dass.ontologycatalog.model.ObjectPropertyNode;
import br.cin.ufpe.dass.ontologycatalog.services.OntologyCatalogService;
import br.cin.ufpe.dass.ontologycatalog.services.exception.OntologyAlreadyImported;
import br.cin.ufpe.dass.ontologycatalog.services.exception.OntologyCatalogException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "Ontology Management")
public class OntologyResource {

    private static final Logger log = LoggerFactory.getLogger(OntologyResource.class);

    private final OntologyCatalogService ontologyCatalogService;

    public OntologyResource(OntologyCatalogService ontologyCatalogService) {
        this.ontologyCatalogService = ontologyCatalogService;
    }

    @PostMapping("ontologies/import")
    @ApiOperation("Import ontology from URI")
    public ResponseEntity<?> importOntologyFromuri(@RequestBody String uri) {
        ResponseEntity<?> response = null;
        IRI sourceIri = IRI.create(URI.create(uri));
        try {
            ontologyCatalogService.importOntologyAsGraph(sourceIri);
            response = ResponseEntity.ok().body(null);
        } catch (OntologyCatalogException | OWLOntologyCreationException e ) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("errorMessage", e.getMessage());
            response = ResponseEntity.badRequest().headers(headers).body(null);
        } catch (OntologyAlreadyImported e) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("errorMessage", e.getMessage());
            response = ResponseEntity.ok().headers(headers).body(null);
        }
        return response;
    }

    @GetMapping("ontologies")
    @ApiOperation("List Ontologies Names")
    public ResponseEntity<?> getOntologies() {
        return ResponseEntity.ok().body(ontologyCatalogService.getOntologyNames());
    }

    @ApiOperation("List Ontologies Class Nodes")
    @GetMapping("ontologies/classes/{ontology}")
    public ResponseEntity<List<ClassNode>> getOntologyClasses(@PathVariable("ontology") String ontologyName) {
        return ResponseEntity.ok().body(ontologyCatalogService.listClassesByOntologyName(ontologyName));
    }

    @ApiOperation("List Ontologies Data Properties")
    @GetMapping("ontologies/data-properties/{ontology}")
    public ResponseEntity<List<DataPropertyNode>> getDataProperties(@PathVariable("ontology") String ontologyName) {
        return ResponseEntity.ok().body(ontologyCatalogService.listDataPropertiesByOntologyName(ontologyName));
    }

    @ApiOperation("List Ontologies Object Properties")
    @GetMapping("ontologies/object-properties/{ontology}")
    public ResponseEntity<List<ObjectPropertyNode>> getObjectProperties(@PathVariable("ontology") String ontologyName) {
        return ResponseEntity.ok().body(ontologyCatalogService.listObjectPropertiesByOntologyName(ontologyName));
    }

    @PutMapping("ontologies/query")
    @ApiOperation("Search Ontologies by Query (Cypher)")
    public ResponseEntity<?> getQueryResult(@RequestBody String cypherQuery) {
        return ResponseEntity.ok().body(ontologyCatalogService.getQueryResult(cypherQuery));
    }


}
