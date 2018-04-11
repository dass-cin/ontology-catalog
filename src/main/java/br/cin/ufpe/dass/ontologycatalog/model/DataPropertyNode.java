package br.cin.ufpe.dass.ontologycatalog.model;

import lombok.*;
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
public class DataPropertyNode {

    @Id
    private String name;

    //More than one domain is a unionOf
    @Relationship(type = "domain")
    private Set<ClassNode> domain = new HashSet<>();

    //range (date type)
    @Relationship(type = "range")
    private Set<String> range = new HashSet<>();

    @Labels
    private List<String> labels = new ArrayList<>();

    public DataPropertyNode(String name) {
        this.name = name;
    }
}
