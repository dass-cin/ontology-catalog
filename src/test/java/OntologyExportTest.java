import br.cin.ufpe.dass.ontologycatalog.OntologyCatalogApplication;
import br.cin.ufpe.dass.ontologycatalog.services.OntologyCatalogService;
import br.cin.ufpe.dass.ontologycatalog.services.OntologyExportService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = OntologyCatalogApplication.class)
@WebAppConfiguration
public class OntologyExportTest {

    @Autowired
    private OntologyExportService ontologyExportService;

    @Autowired
    private OntologyCatalogService ontologyCatalogService;

    @Test
    public void shouldGenerateOntologyFile() throws OWLOntologyCreationException, OWLOntologyStorageException {
        ontologyExportService.exportOntologyAsFile("cmt", "/tmp/ontology.owl");
    }

    @Test
    public void shouldGenerateOntologyFileFromCypher() throws OWLOntologyCreationException, OWLOntologyStorageException {
        String segmentQuery = "MATCH(c1:ClassNode)-[r:isA]-(c2:ClassNode)\n" +
                "WHERE c1.uri = 'http://cmt#Paper' \n" +
                "RETURN c1,TYPE(r),c2\n" +
                "UNION\n" +
                "MATCH(c1:ObjectPropertyNode)-[r]-(c2:ClassNode)\n" +
                "WHERE c2.uri = 'http://cmt#Paper'\n" +
                "RETURN c1,TYPE(r),c2\n" +
                "UNION\n" +
                "MATCH(c1:DataPropertyNode)-[r]-(c2:ClassNode)\n" +
                "WHERE c2.uri = 'http://cmt#Paper'\n" +
                "RETURN c1,TYPE(r),c2\n" +
                "UNION\n" +
                "MATCH(c1:ClassNode)-[r:isA]-(c2:ClassNode)\n" +
                "WHERE c1.uri = 'http://cmt#SubjectArea' \n" +
                "RETURN c1,TYPE(r),c2\n" +
                "UNION\n" +
                "MATCH(c1:ObjectPropertyNode)-[r]-(c2:ClassNode)\n" +
                "WHERE c2.uri = 'http://cmt#SubjectArea'\n" +
                "RETURN c1,TYPE(r),c2\n" +
                "UNION\n" +
                "MATCH(c1:DataPropertyNode)-[r]-(c2:ClassNode)\n" +
                "WHERE c2.uri = 'http://cmt#SubjectArea'\n" +
                "RETURN c1,TYPE(r),c2\n" +
                "UNION\n" +
                "MATCH(c1:DataPropertyNode)-[r:domain]-(c2:ClassNode)\n" +
                "WHERE c1.uri = 'http://cmt#title'\n" +
                "RETURN c1,TYPE(r),c2";
        String ontologyName = "cmt";
        List<Map<String, Object>> segmentResult = ontologyCatalogService.getQueryResult(segmentQuery);
        ontologyExportService.exportOntologyAsFileFromSegment(ontologyName, segmentResult, "/tmp/ontology-segment.owl");
    }


}
