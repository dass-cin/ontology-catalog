package br.cin.ufpe.dass.ontologycatalog.resources.dto;

import java.util.List;

public class ApplicationRequirements {

    private List<String> dataRequirements;

    public List<String> getDataRequirements() {
        return dataRequirements;
    }

    public void setDataRequirements(List<String> dataRequirements) {
        this.dataRequirements = dataRequirements;
    }
}
