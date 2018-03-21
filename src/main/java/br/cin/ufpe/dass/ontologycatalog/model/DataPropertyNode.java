package br.cin.ufpe.dass.ontologycatalog.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;
import java.util.Set;

@NodeEntity
@Getter
@Setter
@NoArgsConstructor
public class DataPropertyNode {

    @Id
    private String name;

    //More than one domain is a unionOf
    @Relationship(type = "domain")
    private Set<ClassNode> domain;

    //range (date type)
    @Relationship(type = "range")
    private Set<String> range;

    @Labels
    private List<String> labels;

    public DataPropertyNode(String name) {
        this.name = name;
    }
}
