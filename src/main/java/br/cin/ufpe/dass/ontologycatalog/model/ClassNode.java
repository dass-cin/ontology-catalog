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
public class ClassNode {

    @Id
    private String name;

    @Labels
    private List<String> labels;

    @Relationship(type = "hasSynonyms")
    private Set<SynonymNode> synonyms;

    public ClassNode() {
    }

    public ClassNode(String name) {
        this.name = name;
    }

    @Relationship(type = "isA")
    private Set<ClassNode> superClasses;

    public Set<ClassNode> getSuperClasses() {
        if (superClasses == null) {
            superClasses = new HashSet<>();
        }
        return superClasses;
    }

    public Set<SynonymNode> getSynonyms() {
        if (synonyms == null) {
            synonyms = new HashSet<>();
        }
        return synonyms;
    }

    public void setSynonyms(Set<SynonymNode> synonyms) {
        this.synonyms = synonyms;
    }

    public void setSuperClasses(Set<ClassNode> superClasses) {
        this.superClasses = superClasses;
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
