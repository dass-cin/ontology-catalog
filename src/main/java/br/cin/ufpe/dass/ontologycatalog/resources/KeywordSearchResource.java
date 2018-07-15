package br.cin.ufpe.dass.ontologycatalog.resources;

import br.cin.ufpe.dass.ontologycatalog.model.OntologyElement;
import br.cin.ufpe.dass.ontologycatalog.services.KeywordSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/")
public class KeywordSearchResource {

    private static final Logger log = LoggerFactory.getLogger(KeywordSearchResource.class);

    private final KeywordSearchService keywordSearchService;

    public KeywordSearchResource(KeywordSearchService keywordSearchService) {
        this.keywordSearchService = keywordSearchService;
    }

    @GetMapping("keyword-search")
    public ResponseEntity<?> searchNodeByKeyWord(@RequestParam("keyword") String keyword) {
        OntologyElement node = keywordSearchService.searchNodeByKeyWord(keyword);
        if (node != null) {
            return ResponseEntity.ok(node);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("keyword-search/{ontologyName}")
    public ResponseEntity<Map<String, Object>> searchNodeByKeyWord(@PathVariable("ontologyName") String ontologyName, @RequestParam("keyword") String keyword) {
        OntologyElement node = keywordSearchService.searchNodeByKeyWord(ontologyName, keyword);
        Map<String, Object> result = new HashMap<>();
        if (node != null) {
            result.put("node", node);
            result.put("type", node.getClass().getSimpleName());
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("keyword-search-list/{ontologyName}")
    public ResponseEntity<List<Map<String, Object>>> searchNodesByKeyWord(@PathVariable("ontologyName") String ontologyName, @RequestParam("keyword") String keyword) {
        List<OntologyElement> nodes = keywordSearchService.searchNodesByKeyword(ontologyName, keyword);
        List<Map<String, Object>> resultList = new ArrayList<>();
        nodes.forEach(node -> {
            Map<String, Object> result = new HashMap<>();
            if (node != null) {
                result.put("node", node);
                result.put("type", node.getClass().getSimpleName());
                resultList.add(result);
            }
        });
        if (resultList.size() == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(resultList);
    }

}
