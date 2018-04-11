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
public class ClassNode {

    @Id
    private String name;

    @Labels
    private List<String> labels = new ArrayList<>();

    @Relationship(type = "hasSynonyms")
    private Set<SynonymNode> synonyms = new HashSet<>();

    public ClassNode(String name) {
        this.name = name;
    }

    @Relationship(type = "isA")
    private Set<ClassNode> superClasses = new HashSet<>();

}
