package br.cin.ufpe.dass.ontologycatalog.repository;

import br.cin.ufpe.dass.ontologycatalog.model.ObjectPropertyNode;
import javafx.beans.property.ObjectProperty;
import org.springframework.data.repository.CrudRepository;

public interface ObjectPropertyRepository extends CrudRepository<ObjectPropertyNode, String> {
}