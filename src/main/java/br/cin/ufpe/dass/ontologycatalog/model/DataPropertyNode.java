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
public class DataPropertyNode implements OntologyElement  {

    @Id
    private String uri;

    private String name;

    //More than one domain is a unionOf
    @Relationship(type = "domain")
    private Set<ClassNode> domain = new HashSet<>();

    //range (date type)
    @Relationship(type = "range")
    private Set<String> range = new HashSet<>();

    @Relationship(type = "hasKeywords")
    private Set<KeywordNode> keywords = new HashSet<>();

    public DataPropertyNode(String uri, String name) {
        this.uri = uri;
        this.name = name;
    }
}
