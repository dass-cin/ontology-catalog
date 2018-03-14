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
public class DataPropertyNode {

    @Id
    private String name;

    //More than one domain is a unionOf
    @Relationship(type = "domain")
    private Set<ClassNode> domain;

    //range (date type)
    @Relationship(type = "range")
    private Set<String> range;

    @Labels
    private List<String> labels;

    public Set<ClassNode> getDomain() {
        if (domain == null) {
            domain = new HashSet<>();
        }
        return domain;
    }

    public void setDomain(Set<ClassNode> domain) {
        this.domain = domain;
    }

    public Set<String> getRange() {
        if (range == null) {
            range = new HashSet<>();
        }
        return range;
    }

    public void setRange(Set<String> range) {
        this.range = range;
    }

    public DataPropertyNode() {
    }

    public DataPropertyNode(String name) {
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
