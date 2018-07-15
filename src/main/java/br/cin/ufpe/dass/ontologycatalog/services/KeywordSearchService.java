package br.cin.ufpe.dass.ontologycatalog.services;

import br.cin.ufpe.dass.ontologycatalog.model.OntologyElement;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

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


    public List<OntologyElement> searchNodesByKeyword(String ontology, String keyword) {
        keyword = keyword.toLowerCase();
        ontology = ontology.substring(0,1).toLowerCase() + ontology.substring(1);

        Map<String, OntologyElement> parameters = new HashMap<>();

        List<OntologyElement> selectedNodes = new ArrayList<>();

//        String baseQuery = String.format("MATCH p=(k:KeywordNode)-[]-(node) WHERE node.name =~'%s' \n", keyword);
//        baseQuery += String.format(" AND node.uri =~'%s' ", ".*" + ontology + ".*");

        String baseQuery = String.format("MATCH p=(k:KeywordNode)-[]-(node) WHERE k.keyword =~'%s' \n", ".*" + keyword + ".*");
        baseQuery += String.format(" AND node.uri =~ '%s' ", "http://"+ontology + ".*");

        String likeCountQuery = baseQuery + " WITH count(distinct(node)) as total RETURN total";

        Result result = session.query(likeCountQuery, parameters, true);

        long totalNodesWithLikeKeyword = (long) result.iterator().next().get("total");

        if (totalNodesWithLikeKeyword > 1) {

            log.info("More than one result -> Returning multiple elements");

            String query = baseQuery + "RETURN distinct(node)";

            result = session.query(query, parameters);
            System.out.println(result.toString());
            //retorna o tipo de nó: ClassNode, PropertyNode, DataPropertyNode
            Iterator<Map<String, Object>> it = result.iterator();
            while(it.hasNext()) {
                selectedNodes.add((OntologyElement) it.next().get("node"));
            }
        } else {
            //Se for só um, retornar ele (ex.: title)
            String query = String.format("MATCH p=(k:KeywordNode)-[]-(node) WHERE k.keyword =~'%s' \n", ".*" + keyword + ".*");
            query += String.format(" AND node.uri =~'%s' ", "http://"+ontology + ".*");
            query +=
                    "RETURN distinct(node)";
            log.info("Step 2: Just one result, returning the first = \n {}", query);
            result = session.query(query, parameters, true);
            if ((result.iterator().hasNext())) {
                selectedNodes.add((OntologyElement) result.iterator().next().get("node"));
            }

        }

        return selectedNodes;

    }

    //@TODO retornar uma lista de elementos ordenada pela betweeness centrality
    public OntologyElement searchNodeByKeyWord(String ontology, String keyword) {

        keyword = keyword.toLowerCase();
        ontology = ontology.toLowerCase();

        //@TODO considerar varios elementos no retorno.

        Map<String, OntologyElement> parameters = new HashMap<>();

        OntologyElement selectedNode = null;

        String baseQuery = String.format("MATCH p=(k:KeywordNode)-[]-(node) WHERE node.name =~'%s' \n", keyword);
        if (ontology != null) {
            baseQuery += String.format(" AND node.uri =~'%s' ", ".*"+ ontology+".*");
        }

//        String countQuery = baseQuery + " WITH count(distinct(node)) as total RETURN total";
//        log.info("Step 1: Executing count query = \n {}", countQuery);
//
//        Result result = session.query(countQuery, parameters, true);
//
//        long totalNodesWithExactKeyword = (long)result.iterator().next().get("total");
//
//        if (totalNodesWithExactKeyword == 1) {
//            //Se já achou o nó com o termo exato, retorna.
//            String returnQuery = baseQuery + " RETURN distinct(node)";
//            result = session.query(returnQuery, parameters, true);
//            if ((result.iterator().hasNext())) {
//                selectedNode = (OntologyElement) result.iterator().next().get("node");
//            }
//        } else {
            //Se não há nenhum nó com o termo exato, busque o termo aproximado
            baseQuery = String.format("MATCH p=(k:KeywordNode)-[]-(node) WHERE k.keyword =~'%s' \n", ".*"+ keyword+".*");
            if (ontology != null) {
                baseQuery += String.format(" AND node.uri =~'%s' ", ".*"+ ontology+".*");
            }

            String likeCountQuery = baseQuery + " WITH count(distinct(node)) as total RETURN total";

            Result result = session.query(likeCountQuery, parameters, true);

            long totalNodesWithLikeKeyword = (long)result.iterator().next().get("total");

            if (totalNodesWithLikeKeyword > 1) {
                //Se for mais de um, rodar betweenness centrality

                String query = String.format("CALL algo.betweenness.stream(\n" +
                        "  'MATCH (node) RETURN id(node) as id',\n" +
                        "  \"MATCH (k:KeywordNode)-[]-(node) WHERE k.keyword =~'%s' MATCH(node)-[r]-(node2) \n", ".*"+ keyword+".*");
                if (ontology != null) {
                    query += String.format(" WHERE node.uri =~'%s' ", ".*"+ ontology+".*");
                }
                query += " RETURN id(node) as source, id(node2) as target\",\n" +
                        "  {graph:'cypher', iterations:5, write: false})\n" +
                        "  YIELD nodeId, centrality\n" +
                        "  RETURN nodeId, centrality \n" +
                        "  ORDER BY centrality DESC LIMIT 1";

                log.info("More than one result -> Calculating betweeness centrality = \n {}", query);

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
                String query = String.format("MATCH p=(k:KeywordNode)-[]-(node) WHERE k.keyword =~'%s' \n", ".*"+ keyword+".*");
                if (ontology != null) {
                    query += String.format(" AND node.uri =~'%s' ", ontology);
                }
                query +=
                        "RETURN distinct(node)";
                log.info("Step 2: Just one result, returning the first = \n {}", query);
                result = session.query(query, parameters, true);
                if ((result.iterator().hasNext())) {
                    selectedNode = (OntologyElement) result.iterator().next().get("node");
                }
//            }

        }


        return selectedNode;
    }

}
