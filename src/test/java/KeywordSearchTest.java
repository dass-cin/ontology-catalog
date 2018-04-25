import br.cin.ufpe.dass.ontologycatalog.OntologyCatalogApplication;
import br.cin.ufpe.dass.ontologycatalog.model.ClassNode;
import br.cin.ufpe.dass.ontologycatalog.model.DataPropertyNode;
import br.cin.ufpe.dass.ontologycatalog.model.OntologyElement;
import br.cin.ufpe.dass.ontologycatalog.services.KeywordSearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.neo4j.ogm.session.Session;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static junit.framework.TestCase.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = OntologyCatalogApplication.class)
@WebAppConfiguration
public class KeywordSearchTest {

    @Inject
    private KeywordSearchService keywordSearchService;

    @Inject
    private Session session;

    private MockMvc mockMvc;

    private static String ONTOLOGY = "cmt";

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        KeywordSearchService keywordSearchService = new KeywordSearchService(session);
        this.mockMvc = MockMvcBuilders.standaloneSetup(keywordSearchService).build();
    }

    @Test
    public void shouldSearchKeyWordTitle() {

        String keyword = "title";

        OntologyElement title = keywordSearchService.searchNodeByKeyWord(ONTOLOGY, keyword);

        assertTrue(title != null);
        assertTrue(title instanceof DataPropertyNode);
        assertTrue(((DataPropertyNode)title).getName().equals("title"));

    }

    @Test
    public void shouldSearchKeywordPerson() {
        String keyword = "person";
        OntologyElement person = keywordSearchService.searchNodeByKeyWord(ONTOLOGY, keyword);

        assertTrue(person != null);
        assertTrue(person instanceof ClassNode);
        assertTrue(((ClassNode)person).getName().equals("Person"));
    }

    @Test
    public void shouldSearchKeywordSubjectArea() {
        String keyword = "subject area";
        OntologyElement subjectArea = keywordSearchService.searchNodeByKeyWord(ONTOLOGY, keyword);

        assertTrue(subjectArea != null);
        assertTrue(subjectArea instanceof ClassNode);
        assertTrue(((ClassNode)subjectArea).getName().equals("SubjectArea"));
    }


}

