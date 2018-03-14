package br.cin.ufpe.dass.ontologycatalog.repository;

import br.cin.ufpe.dass.ontologycatalog.model.DataPropertyNode;
import org.springframework.data.repository.CrudRepository;

public interface DataPropertyRepository extends CrudRepository<DataPropertyNode, String> {
}