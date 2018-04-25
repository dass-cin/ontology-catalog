package br.cin.ufpe.dass.ontologycatalog.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import org.neo4j.ogm.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NodeEntity
@Getter
@Setter
@NoArgsConstructor
public class ObjectPropertyNode implements OntologyElement {

    @Id
    private String uri;

    private String name;

    //More than one range is a unionOf
    @Relationship(type = "range")
    private Set<ClassNode> range = new HashSet<>();

    //More than one domain is a unionOf
    @Relationship(type = "domain")
    private Set<ClassNode> domain = new HashSet<>();

    @Relationship(type = "hasKeywords")
    private Set<KeywordNode> keywords = new HashSet<>();

    public ObjectPropertyNode(String uri, String name) {
        this.uri = uri;
        this.name = name;
    }

}
