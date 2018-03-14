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

    public Set<ClassNode> getRange() {
        if (range == null) {
            range = new HashSet<>();
        }
        return range;
    }

    public void setRange(Set<ClassNode> range) {
        this.range = new HashSet<>();
    }

    public Set<ClassNode> getDomain() {
        if (domain == null) {
            this.domain = new HashSet<>();
        }
        return domain;
    }

    public void setDomain(Set<ClassNode> domain) {
        this.domain = domain;
    }

    public ObjectPropertyNode() {
    }

    public ObjectPropertyNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLabels() {
        if (labels == null) {
            labels = new ArrayList<>();
        }
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
}
