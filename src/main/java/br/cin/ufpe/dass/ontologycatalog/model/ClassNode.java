package br.cin.ufpe.dass.ontologycatalog.model;

import lombok.*;
import org.neo4j.ogm.annotation.*;

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
    private String uri;

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
