package br.cin.ufpe.dass.ontologycatalog.repository;

import br.cin.ufpe.dass.ontologycatalog.model.ClassNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.stream.Stream;

public interface ClassNodeRepositoryCustom {

    Stream<Map<String, Object>> getQueryResult(String cypherQuery);

}