package br.cin.ufpe.dass.ontologycatalog;

import br.cin.ufpe.dass.ontologycatalog.config.ApplicationConfig;
import br.cin.ufpe.dass.ontologycatalog.services.OntologyCatalogService;
import edu.smu.tspell.wordnet.WordNetDatabase;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.net.URI;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationConfig.class})
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class OntologyCatalogApplication {

	private final ApplicationConfig applicationConfig;

	private static final Logger log = LoggerFactory.getLogger(OntologyCatalogApplication.class);

	public OntologyCatalogApplication(ApplicationConfig applicationConfig) {
		this.applicationConfig = applicationConfig;
	}

	public static void main(String[] args) {
		SpringApplication.run(OntologyCatalogApplication.class, args);
	}

	@Bean
	CommandLineRunner run(OntologyCatalogService ontologyCatalogService) {
		return args -> {
			if (ontologyCatalogService.getOntologyUris().isEmpty()) {
				IRI sourceIri = IRI.create(URI.create("file:///Users/diegopessoa/Projects/phd/ontologies/conference/cmt.owl"));
				ontologyCatalogService.importOntologyAsGraph(sourceIri);
				sourceIri = IRI.create(URI.create("file:///Users/diegopessoa/Projects/phd/ontologies/conference/Conference.owl"));
				ontologyCatalogService.importOntologyAsGraph(sourceIri);
				sourceIri = IRI.create(URI.create("file:///Users/diegopessoa/Projects/phd/ontologies/conference/confOf.owl"));
				ontologyCatalogService.importOntologyAsGraph(sourceIri);
				sourceIri = IRI.create(URI.create("file:///Users/diegopessoa/Projects/phd/ontologies/conference/edas.owl"));
				ontologyCatalogService.importOntologyAsGraph(sourceIri);
				sourceIri = IRI.create(URI.create("file:///Users/diegopessoa/Projects/phd/ontologies/conference/iasted.owl"));
				ontologyCatalogService.importOntologyAsGraph(sourceIri);
				sourceIri = IRI.create(URI.create("file:///Users/diegopessoa/Projects/phd/ontologies/conference/sigkdd.owl"));
				ontologyCatalogService.importOntologyAsGraph(sourceIri);
				sourceIri = IRI.create(URI.create("file:///Users/diegopessoa/Projects/phd/ontologies/conference/ekaw.owl"));
				ontologyCatalogService.importOntologyAsGraph(sourceIri);
			}
		};
	}

	@Bean
	public WordNetDatabase wordNetDatabase() {
		// Initialize the WordNet interface.

		WordNetDatabase wordNet = null;

		String wordnetdir = applicationConfig.getInitialization().getWordnetDir();

		System.setProperty("wordnet.database.dir", wordnetdir);
		// Instantiate wordnet.
		try {
			wordNet = WordNetDatabase.getFileInstance();
		} catch (Exception e) {
			log.error("Failed to start wordnet database");
		}

		log.info("Wordnet database initialized");

		return wordNet;
	}

}
