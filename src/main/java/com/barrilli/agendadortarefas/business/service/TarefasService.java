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

        tarefasDTO.setDataCriacao(LocalDateTime.now());
        tarefasDTO.setStatusNotificacao(StatusNotificacaoEnum.PENDENTE);
        tarefasDTO.setEmailUsuario(email);

        var tarefa = tarefasConverter.paraTarefasEntity(tarefasDTO);

        return tarefasConverter.paraTarefasDTO(tarefaRepository.save(tarefa));
    }

    public List<TarefasDTO> buscaTarefasAgendadasPorPeriodo(LocalDateTime dataInicial, LocalDateTime dataFinal) {
        return tarefasConverter.paraListaTarefasDTO(
                tarefaRepository.findByDataEventoBetween(dataInicial, dataFinal));
    }

    public List<TarefasDTO> buscaTarefasPorEmail(String token) {
        String email = jwtUtil.extractUsername(token.substring(7));

        List<TarefasEntity> listaTarefas = tarefaRepository.findByEmailUsuario(email);

        return tarefasConverter.paraListaTarefasDTO(listaTarefas);
    }

    public void deletarTarefaPorId(String id) {
        try {
            tarefaRepository.deleteById(id);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Erro ao deletar tarefas para id: " + id, e.getCause());
        }
    }

    public TarefasDTO alteraStatus(StatusNotificacaoEnum statusNotificacao, String id) {
        try {
            var entity = tarefaRepository.findById(id)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Tarefa não encontrada " + id)
                    );
            entity.setStatusNotificacao(statusNotificacao);

            return tarefasConverter.paraTarefasDTO(tarefaRepository.save(entity));
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Erro ao alterar status da tarefa para id: " + id, e.getCause());
        }
    }

    public TarefasDTO updateTarefa(TarefasDTO tarefasDTO, String id) {
        try {
            var entity = tarefaRepository.findById(id)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Tarefa não encontrada " + id)
                    );
            tarefaUpdateConverter.updateTarefas(tarefasDTO, entity);

            return tarefasConverter.paraTarefasDTO(tarefaRepository.save(entity));
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Erro ao alterar tarefa para id: " + id, e.getCause());
        }
    }
}
