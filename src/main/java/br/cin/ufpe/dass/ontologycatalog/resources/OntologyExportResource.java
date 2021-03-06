package br.cin.ufpe.dass.ontologycatalog.resources;

import br.cin.ufpe.dass.ontologycatalog.services.OntologyCatalogService;
import br.cin.ufpe.dass.ontologycatalog.services.OntologyExportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/")
@Api(tags = "Ontology Exporting")
public class OntologyExportResource {

    private static final Logger log = LoggerFactory.getLogger(OntologyExportResource.class);

    private final OntologyExportService ontologyExportService;

    private final OntologyCatalogService ontologyCatalogService;

    public OntologyExportResource(OntologyExportService ontologyExportService, OntologyCatalogService ontologyCatalogService) {
        this.ontologyExportService = ontologyExportService;
        this.ontologyCatalogService = ontologyCatalogService;
    }

    @PutMapping("/ontology-export")
    @ApiOperation("Export ontology as OWL")
    public ResponseEntity<String> exportOntologySegmentAsFile(@RequestBody String segmentationQuery, @RequestParam("ontologyName") String ontologyName, @RequestParam("filepath") String filePath) {
        List<Map<String, Object>> segmentResult = ontologyCatalogService.getQueryResult(segmentationQuery);
        if (segmentResult.size() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        try {
            ontologyExportService.exportOntologyAsFileFromSegment(ontologyName, segmentResult, filePath);
        } catch (OWLOntologyCreationException | OWLOntologyStorageException  e) {
            log.error("Fail to export segment result to a file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok().body(filePath);
    }

}
