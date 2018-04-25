package br.cin.ufpe.dass.ontologycatalog.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
@Getter
@Setter
@NoArgsConstructor
public class KeywordNode {

    @Id
    private String keyword;

    public KeywordNode(String keyword) {
        this.keyword = keyword;
    }
}
