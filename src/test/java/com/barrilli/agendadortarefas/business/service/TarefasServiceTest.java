package com.barrilli.agendadortarefas.business.service;

import com.barrilli.agendadortarefas.business.dto.TarefasDTO;
import com.barrilli.agendadortarefas.business.mapper.TarefaUpdateConverter;
import com.barrilli.agendadortarefas.business.mapper.TarefaUpdateConverterImpl;
import com.barrilli.agendadortarefas.business.mapper.TarefasConverter;
import com.barrilli.agendadortarefas.infra.enums.StatusNotificacaoEnum;
import com.barrilli.agendadortarefas.infra.exception.ResourceNotFoundException;
import com.barrilli.agendadortarefas.infra.model.TarefasEntity;
import com.barrilli.agendadortarefas.infra.repository.TarefasRepository;
import com.barrilli.agendadortarefas.infra.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarefasServiceTest {

    private static final String TOKEN = "Bearer abc";

    @Mock
    TarefasRepository repository;

    @Mock
    TarefasConverter converter;

    // MapStruct real: a proteção de id e emailUsuario no PUT mora nos @Mapping(ignore) do converter
    @Spy
    TarefaUpdateConverter updateConverter = new TarefaUpdateConverterImpl();

    @Mock
    JwtUtil jwtUtil;

    @InjectMocks
    TarefasService service;

    @BeforeEach
    void setUp() {
        lenient().when(jwtUtil.extractUsername("abc")).thenReturn("ana@t.com");
    }

    private TarefasEntity tarefaDe(String email) {
        return TarefasEntity.builder().id("t1").emailUsuario(email).statusNotificacao(StatusNotificacaoEnum.PENDENTE).build();
    }

    @Test
    void gravarTarefa_defineDonoStatusEDataEIgnoraIdDoCliente() {
        var dto = TarefasDTO.builder().id("id-do-cliente").nomeTarefa("Ligar").emailUsuario("outro@t.com")
                .dataEvento(LocalDateTime.now().plusDays(1)).build();
        var entity = new TarefasEntity();
        when(converter.paraTarefasEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(converter.paraTarefasDTO(entity)).thenReturn(dto);

        service.gravarTarefa(TOKEN, dto);

        assertThat(dto.getId()).isNull();
        assertThat(dto.getEmailUsuario()).isEqualTo("ana@t.com");
        assertThat(dto.getStatusNotificacao()).isEqualTo(StatusNotificacaoEnum.PENDENTE);
        assertThat(dto.getDataCriacao()).isNotNull();
    }

    @Test
    void buscaTarefasPorEmail_usaOEmailDoToken() {
        var entities = List.of(tarefaDe("ana@t.com"));
        when(repository.findByEmailUsuario("ana@t.com")).thenReturn(entities);
        when(converter.paraListaTarefasDTO(entities)).thenReturn(List.of(new TarefasDTO()));

        assertThat(service.buscaTarefasPorEmail(TOKEN)).hasSize(1);
    }

    @Test
    void buscaTarefasAgendadasPorPeriodo_buscaSoPendentes() {
        var inicio = LocalDateTime.now();
        var fim = inicio.plusMinutes(5);

        service.buscaTarefasAgendadasPorPeriodo(inicio, fim);

        verify(repository).findByDataEventoBetweenAndStatusNotificacao(inicio, fim, StatusNotificacaoEnum.PENDENTE);
    }

    @Test
    void deletarTarefa_doProprioUsuario_apaga() {
        var tarefa = tarefaDe("ana@t.com");
        when(repository.findById("t1")).thenReturn(Optional.of(tarefa));

        service.deletarTarefaPorId(TOKEN, "t1");

        verify(repository).delete(tarefa);
    }

    @Test
    void deletarTarefa_deOutroUsuario_lanca404ENaoApaga() {
        when(repository.findById("t1")).thenReturn(Optional.of(tarefaDe("outro@t.com")));

        assertThatThrownBy(() -> service.deletarTarefaPorId(TOKEN, "t1"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    void alteraStatus_doProprioUsuario_salvaNovoStatus() {
        var tarefa = tarefaDe("ana@t.com");
        when(repository.findById("t1")).thenReturn(Optional.of(tarefa));
        when(repository.save(tarefa)).thenReturn(tarefa);

        service.alteraStatus(TOKEN, StatusNotificacaoEnum.CANCELADO, "t1");

        assertThat(tarefa.getStatusNotificacao()).isEqualTo(StatusNotificacaoEnum.CANCELADO);
    }

    @Test
    void alteraStatus_deOutroUsuario_lanca404() {
        when(repository.findById("t1")).thenReturn(Optional.of(tarefaDe("outro@t.com")));

        assertThatThrownBy(() -> service.alteraStatus(TOKEN, StatusNotificacaoEnum.CANCELADO, "t1"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void alteraStatusInterno_naoConfereDono() {
        var tarefa = tarefaDe("outro@t.com");
        when(repository.findById("t1")).thenReturn(Optional.of(tarefa));
        when(repository.save(tarefa)).thenReturn(tarefa);

        service.alteraStatus(StatusNotificacaoEnum.NOTIFICADO, "t1");

        assertThat(tarefa.getStatusNotificacao()).isEqualTo(StatusNotificacaoEnum.NOTIFICADO);
    }

    @Test
    void alteraStatusInterno_tarefaInexistente_lanca404() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.alteraStatus(StatusNotificacaoEnum.NOTIFICADO, "x"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateTarefa_doProprioUsuario_aplicaAlteracoesSemTrocarIdNemDono() {
        var tarefa = tarefaDe("ana@t.com");
        var dto = TarefasDTO.builder().id("outro-id").emailUsuario("outro@t.com").nomeTarefa("Novo nome").build();
        when(repository.findById("t1")).thenReturn(Optional.of(tarefa));
        when(repository.save(tarefa)).thenReturn(tarefa);

        service.updateTarefa(TOKEN, dto, "t1");

        assertThat(tarefa.getNomeTarefa()).isEqualTo("Novo nome");
        assertThat(tarefa.getId()).isEqualTo("t1");
        assertThat(tarefa.getEmailUsuario()).isEqualTo("ana@t.com");
        verify(repository).save(tarefa);
    }

    @Test
    void updateTarefa_deOutroUsuario_lanca404() {
        when(repository.findById("t1")).thenReturn(Optional.of(tarefaDe("outro@t.com")));

        assertThatThrownBy(() -> service.updateTarefa(TOKEN, new TarefasDTO(), "t1"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(updateConverter, never()).updateTarefas(any(), any());
    }

    @Test
    void tarefaAntigaSemEmailUsuario_lanca404ENaoNullPointer() {
        when(repository.findById("t1")).thenReturn(Optional.of(tarefaDe(null)));

        assertThatThrownBy(() -> service.deletarTarefaPorId(TOKEN, "t1"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).delete(any());
    }
}
