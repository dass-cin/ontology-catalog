package br.cin.ufpe.dass.ontologycatalog.repository;

import br.cin.ufpe.dass.ontologycatalog.model.OntologyNode;
import org.springframework.data.repository.CrudRepository;

public interface OntologyNodeRepository extends CrudRepository<OntologyNode, String> {
}