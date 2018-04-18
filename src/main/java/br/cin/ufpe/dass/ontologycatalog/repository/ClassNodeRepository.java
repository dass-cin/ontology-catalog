package br.cin.ufpe.dass.ontologycatalog.repository;

import br.cin.ufpe.dass.ontologycatalog.model.ClassNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface ClassNodeRepository extends CrudRepository<ClassNode, String> {

    @Query("MATCH (c:ClassNode) RETURN distinct(split(c.uri,'#')[0])")
    Stream<String> getDistinctURIs();

    @Query("MATCH (c:ClassNode)-[p]-(c1:ClassNode) WHERE c.uri =~{ontologyName} RETURN c,p,c1 ORDER BY c.name ASC")
    Stream<ClassNode> listAllByOntologyName(@Param("ontologyName") String ontologyName);

}