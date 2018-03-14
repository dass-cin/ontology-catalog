package br.cin.ufpe.dass.ontologycatalog.model;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NodeEntity
public class OntologyNode {

    @Id
    private String uri;

    private String name;

    private String version;

    @Relationship(type = "hasClasses")
    private Set<ClassNode> classes;

    private String comment;

    @Labels
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

    public Set<ClassNode> getClasses() {
        if (classes == null) {
            classes = new HashSet<>();
        }
        return classes;
    }

    public void setClasses(Set<ClassNode> classes) {
        this.classes = classes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        if (labels == null) {
            labels = new ArrayList<>();
        }
        this.labels = labels;
    }
}
