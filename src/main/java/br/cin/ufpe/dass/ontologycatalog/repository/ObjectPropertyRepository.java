package br.cin.ufpe.dass.ontologycatalog.repository;

import br.cin.ufpe.dass.ontologycatalog.model.ObjectPropertyNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.stream.Stream;

public interface ObjectPropertyRepository extends CrudRepository<ObjectPropertyNode, String> {

    @Query("MATCH (p1:ObjectPropertyNode)-[r]-(p2:ObjectPropertyNode) WHERE p1.uri =~{ontologyName} RETURN p1,r,p2 ORDER BY p1.name ASC")
    Stream<ObjectPropertyNode> listAllByOntologyName(@Param("ontologyName") String ontologyName);
}