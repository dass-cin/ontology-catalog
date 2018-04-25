package br.cin.ufpe.dass.ontologycatalog.repository;

import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ClassNodeRepositoryCustomImpl implements ClassNodeRepositoryCustom {

    private final Session session;

    public ClassNodeRepositoryCustomImpl(Session session) {
        this.session = session;
    }


    public Stream<Map<String, Object>> getQueryResult(String cypherQuery) {
        Result result = session.query(cypherQuery, new HashMap<String, String>());
        return StreamSupport.stream(result.spliterator(), true);
    }

}