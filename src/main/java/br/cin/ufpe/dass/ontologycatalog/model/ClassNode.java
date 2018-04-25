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
public class ClassNode implements OntologyElement {

    @Id
    private String uri;

    private String name;

    @Relationship(type = "hasKeywords")
    private Set<KeywordNode> keywords = new HashSet<>();

    public ClassNode(String name) {
        this.name = name;
    }

    @Relationship(type = "isA")
    private Set<ClassNode> superClasses = new HashSet<>();

}
