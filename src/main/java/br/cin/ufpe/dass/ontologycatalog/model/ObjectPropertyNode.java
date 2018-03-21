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
public class ObjectPropertyNode {

    @Id
    private String name;

    //More than one range is a unionOf
    @Relationship(type = "range")
    private Set<ClassNode> range;

    //More than one domain is a unionOf
    @Relationship(type = "domain")
    private Set<ClassNode> domain;

    @Labels
    private List<String> labels;

    public ObjectPropertyNode(String name) {
        this.name = name;
    }
}
