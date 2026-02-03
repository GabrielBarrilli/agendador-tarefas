package com.barrilli.agendadortarefas.infra.repository;

import com.barrilli.agendadortarefas.infra.enums.StatusNotificacaoEnum;
import com.barrilli.agendadortarefas.infra.model.TarefasEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TarefasRepository extends MongoRepository<TarefasEntity, String> {

    List<TarefasEntity> findByDataEventoBetweenAndStatusNotificacao(LocalDateTime dataInicial,
                                                                    LocalDateTime dataFinal,
                                                                    StatusNotificacaoEnum statusNotificacao);

    List<TarefasEntity> findByEmailUsuario(String email);

}