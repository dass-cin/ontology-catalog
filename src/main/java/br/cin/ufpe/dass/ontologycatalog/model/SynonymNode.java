package br.cin.ufpe.dass.ontologycatalog.model;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class SynonymNode {

    @Id
    private String name;

    public SynonymNode() {
    }

    public SynonymNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
