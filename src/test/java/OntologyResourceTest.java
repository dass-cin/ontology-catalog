import br.cin.ufpe.dass.ontologycatalog.OntologyCatalogApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(JUnit4.class)
@SpringBootTest(classes = OntologyCatalogApplication.class)
@WebAppConfiguration
public class OntologyResourceTest {

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restFileTransferMockMvc;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restFileTransferMockMvc = MockMvcBuilders.standaloneSetup().setCustomArgumentResolvers(pageableArgumentResolver)
                .setMessageConverters(jacksonMessageConverter).build();
    }

    @Test
    public void shouldListURIs() throws Exception {
        restFileTransferMockMvc.perform(get("/api/ontologies"))
                .andExpect(status().isOk());
    }

}
