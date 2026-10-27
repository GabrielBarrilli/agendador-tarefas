package com.barrilli.agendadortarefas.infra.repository;

import com.barrilli.agendadortarefas.infra.enums.StatusNotificacaoEnum;
import com.barrilli.agendadortarefas.infra.model.TarefasEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
class TarefasRepositoryTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @Autowired
    TarefasRepository repository;

    // @DataMongoTest não desfaz as gravações: sem isso, o resultado dependeria da ordem dos testes
    @BeforeEach
    void limpa() {
        repository.deleteAll();
    }

    @Test
    void buscaSoPendentesDentroDoPeriodo() {
        var agora = LocalDateTime.now().withNano(0);
        repository.saveAll(List.of(
                tarefa("dentro", "ana@t.com", agora.plusMinutes(62), StatusNotificacaoEnum.PENDENTE),
                tarefa("fora", "ana@t.com", agora.plusHours(3), StatusNotificacaoEnum.PENDENTE),
                tarefa("notificada", "ana@t.com", agora.plusMinutes(62), StatusNotificacaoEnum.NOTIFICADO)));

        var resultado = repository.findByDataEventoBetweenAndStatusNotificacao(
                agora.plusHours(1), agora.plusMinutes(65), StatusNotificacaoEnum.PENDENTE);

        assertThat(resultado).extracting(TarefasEntity::getNomeTarefa).containsExactly("dentro");
    }

    @Test
    void buscaPorEmailTrazSoAsDoUsuario() {
        repository.saveAll(List.of(
                tarefa("minha", "ana@t.com", LocalDateTime.now(), StatusNotificacaoEnum.PENDENTE),
                tarefa("dele", "bob@t.com", LocalDateTime.now(), StatusNotificacaoEnum.PENDENTE)));

        assertThat(repository.findByEmailUsuario("ana@t.com"))
                .extracting(TarefasEntity::getNomeTarefa).containsExactly("minha");
    }

    private TarefasEntity tarefa(String nome, String email, LocalDateTime data, StatusNotificacaoEnum status) {
        return TarefasEntity.builder().nomeTarefa(nome).emailUsuario(email).dataEvento(data).statusNotificacao(status).build();
    }
}
