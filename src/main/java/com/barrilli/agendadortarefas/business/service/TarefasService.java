package com.barrilli.agendadortarefas.business.service;

import com.barrilli.agendadortarefas.business.dto.TarefasDTO;
import com.barrilli.agendadortarefas.business.mapper.TarefaUpdateConverter;
import com.barrilli.agendadortarefas.business.mapper.TarefasConverter;
import com.barrilli.agendadortarefas.infra.enums.StatusNotificacaoEnum;
import com.barrilli.agendadortarefas.infra.exception.ResourceNotFoundException;
import com.barrilli.agendadortarefas.infra.model.TarefasEntity;
import com.barrilli.agendadortarefas.infra.repository.TarefasRepository;
import com.barrilli.agendadortarefas.infra.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarefasService {

    private final TarefasRepository tarefaRepository;
    private final TarefasConverter tarefasConverter;
    private final TarefaUpdateConverter tarefaUpdateConverter;
    private final JwtUtil jwtUtil;

    public TarefasDTO gravarTarefa(String token, TarefasDTO tarefasDTO) {
        String email = jwtUtil.extractUsername(token.substring(7));

        tarefasDTO.setId(null);
        tarefasDTO.setDataCriacao(LocalDateTime.now());
        tarefasDTO.setStatusNotificacao(StatusNotificacaoEnum.PENDENTE);
        tarefasDTO.setEmailUsuario(email);

        var tarefa = tarefasConverter.paraTarefasEntity(tarefasDTO);

        return tarefasConverter.paraTarefasDTO(tarefaRepository.save(tarefa));
    }

    public List<TarefasDTO> buscaTarefasAgendadasPorPeriodo(LocalDateTime dataInicial, LocalDateTime dataFinal) {
        return tarefasConverter.paraListaTarefasDTO(
                tarefaRepository.findByDataEventoBetweenAndStatusNotificacao(dataInicial, dataFinal, StatusNotificacaoEnum.PENDENTE));
    }

    public List<TarefasDTO> buscaTarefasPorEmail(String token) {
        String email = jwtUtil.extractUsername(token.substring(7));

        List<TarefasEntity> listaTarefas = tarefaRepository.findByEmailUsuario(email);

        return tarefasConverter.paraListaTarefasDTO(listaTarefas);
    }

    public void deletarTarefaPorId(String token, String id) {
        tarefaRepository.delete(buscaTarefaDoUsuario(token, id));
    }

    public TarefasDTO alteraStatus(String token, StatusNotificacaoEnum statusNotificacao, String id) {
        return salvaStatus(buscaTarefaDoUsuario(token, id), statusNotificacao);
    }

    // Uso interno (job de notificação): não há usuário, então não confere o dono
    public TarefasDTO alteraStatus(StatusNotificacaoEnum statusNotificacao, String id) {
        var entity = tarefaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + id));
        return salvaStatus(entity, statusNotificacao);
    }

    public TarefasDTO updateTarefa(String token, TarefasDTO tarefasDTO, String id) {
        var entity = buscaTarefaDoUsuario(token, id);
        tarefaUpdateConverter.updateTarefas(tarefasDTO, entity);

        return tarefasConverter.paraTarefasDTO(tarefaRepository.save(entity));
    }

    private TarefasDTO salvaStatus(TarefasEntity entity, StatusNotificacaoEnum statusNotificacao) {
        entity.setStatusNotificacao(statusNotificacao);
        return tarefasConverter.paraTarefasDTO(tarefaRepository.save(entity));
    }

    // Tarefa de outro usuário responde 404, para não revelar que o id existe
    private TarefasEntity buscaTarefaDoUsuario(String token, String id) {
        String email = jwtUtil.extractUsername(token.substring(7));
        return tarefaRepository.findById(id)
                .filter(tarefa -> email.equals(tarefa.getEmailUsuario()))
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + id));
    }
}
