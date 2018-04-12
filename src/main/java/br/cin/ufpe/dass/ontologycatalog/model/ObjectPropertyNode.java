package br.cin.ufpe.dass.ontologycatalog.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NodeEntity
@Getter
@Setter
@NoArgsConstructor
public class ObjectPropertyNode {

    @Id
    private String uri;

    private String name;

    //More than one range is a unionOf
    @Relationship(type = "range")
    private Set<ClassNode> range = new HashSet<>();

    //More than one domain is a unionOf
    @Relationship(type = "domain")
    private Set<ClassNode> domain = new HashSet<>();

    @Labels
    private List<String> labels = new ArrayList<>();

    public ObjectPropertyNode(String name) {
        this.name = name;
    }

    public ObjectPropertyNode(String uri, String name) {
        this.uri = uri;
        this.name = name;
    }
}
