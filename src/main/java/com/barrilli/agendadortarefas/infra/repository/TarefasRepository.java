package com.barrilli.agendadortarefas.infra.repository;

import com.barrilli.agendadortarefas.infra.model.TarefasEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarefasRepository extends MongoRepository<TarefasEntity, String> {
}
