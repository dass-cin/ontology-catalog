package br.cin.ufpe.dass.ontologycatalog.model;

import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NodeEntity
@Getter
@Setter
public class OntologyNode {

    @Id
    private String uri;

    private String name;

    private String version;

    @Relationship(type = "hasClasses")
    private Set<ClassNode> classes = new HashSet<>();

    private String comment;

    @Labels
    @Singular
    private List<String> labels;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
