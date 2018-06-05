package br.cin.ufpe.dass.ontologycatalog.services;

import br.cin.ufpe.dass.ontologycatalog.model.OntologyElement;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class KeywordSearchService {

    private Logger log = LoggerFactory.getLogger(KeywordSearchService.class);

    private final Session session;

    public KeywordSearchService(Session session) {
        this.session = session;
    }


    public OntologyElement searchNodeByKeyWord(String keyword) {
        return this.searchNodeByKeyWord(null, keyword);
    }

    public OntologyElement searchNodeByKeyWord(String ontology, String keyword) {

        Map<String, OntologyElement> parameters = new HashMap<>();

        OntologyElement selectedNode = null;

        // 1) saber quantos nós no grafo possuem essa keyword
        String query = String.format("MATCH p=(k:KeywordNode)-[]-(node) WHERE k.keyword =~'.*%s.*' \n", keyword);
        if (ontology != null) {
            query += String.format(" AND node.uri =~'.*%s.*' ", ontology);
        }
        query +=  "WITH count(distinct(node)) as total RETURN total";
        log.info("Step 1: Executing count query = \n {}", query);
        Result result = session.query(query, parameters, true);
        long totalNodesWithKeyword = (long)result.iterator().next().get("total");

        if (totalNodesWithKeyword > 1) {
            // 2) Se for mais de um, rodar betweenness centrality

            query = String.format("CALL algo.betweenness.stream(\n" +
                    "  'MATCH (node) RETURN id(node) as id',\n" +
                    "  \"MATCH (k:KeywordNode)-[]-(node) WHERE k.keyword =~'.*%s.*' MATCH(node)-[r]-(node2) \n", keyword);
            if (ontology != null) {
                query += String.format(" WHERE node.uri =~'.*%s.*' ", ontology);
            }
            query += " RETURN id(node) as source, id(node2) as target\",\n" +
                    "  {graph:'cypher', iterations:5, write: false})\n" +
                    "  YIELD nodeId, centrality\n" +
                    "  RETURN nodeId, centrality \n" +
                    "  ORDER BY centrality DESC LIMIT 1";

            log.info("Step 2: More than one result -> Calculating betweeness centrality = \n {}", query);

            result = session.query(query, parameters, true);
            Long nodeId = (long)result.iterator().next().get("nodeId");
            System.out.println(String.format("Node id = %d", nodeId));
            //Search node by Node id
            query = String.format("MATCH(node) WHERE ID(node) = %d RETURN node", nodeId);
            result = session.query(query, parameters);
            System.out.println(result.toString());
            //retorna o tipo de nó: ClassNode, PropertyNode, DataPropertyNode
            selectedNode = (OntologyElement) result.iterator().next().get("node");
        } else {
            //Se for só um, retornar ele (ex.: title)
            query = String.format("MATCH p=(k:KeywordNode)-[]-(node) WHERE k.keyword =~'.*%s.*' \n", keyword);
            if (ontology != null) {
                query += String.format(" AND node.uri =~'.*%s.*' ", ontology);
            }
            query +=
                    "RETURN distinct(node)";
            log.info("Step 2: Just one result, returning the first = \n {}", query);
            result = session.query(query, parameters, true);
            if ((result.iterator().hasNext())) {
                selectedNode = (OntologyElement) result.iterator().next().get("node");
            }
        }

        return selectedNode;
    }

}
