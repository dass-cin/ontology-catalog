package br.cin.ufpe.dass.ontologycatalog.repository;

import br.cin.ufpe.dass.ontologycatalog.model.DataPropertyNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.stream.Stream;

public interface DataPropertyRepository extends CrudRepository<DataPropertyNode, String> {

    @Query("MATCH (p1:DataPropertyNode)-[r]-(node) WHERE p1.uri =~{ontologyName} RETURN p1,r,node ORDER BY p1.name ASC")
    Stream<DataPropertyNode> listAllByOntologyName(@Param("ontologyName") String ontologyName);
}