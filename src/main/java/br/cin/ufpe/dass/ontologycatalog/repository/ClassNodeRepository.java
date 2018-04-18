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

    @Query("MATCH (c:ClassNode) WHERE c.uri =~{ontologyName} RETURN c ORDER BY c.name ASC")
    Stream<ClassNode> listAllByOntologyName(@Param("ontologyName") String ontologyName);

    @Query("MATCH (class:ClassNode)-[:isA]-(superClass:ClassNode) WHERE class.uri =~{ontologyName} AND class.name = {className} RETURN class,superClass ORDER BY class.name,superClass.name ASC")
    Iterable<Map<String, ClassNode>> listSuperClassesByOntologyName(@Param("ontologyName") String ontologyName, @Param("className")  String className);

    @Query("MATCH (class:ClassNode)-[:isA]-(superClass:ClassNode) WHERE class.uri =~{ontologyName} RETURN class,superClass ORDER BY class.name,superClass.name ASC")
    Iterable<Map<String, ClassNode>> listSuperClassesByOntologyName(@Param("ontologyName") String ontologyName);
}