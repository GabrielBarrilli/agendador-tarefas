package com.barrilli.agendadortarefas.business.controller;

import com.barrilli.agendadortarefas.business.dto.TarefasDTO;
import com.barrilli.agendadortarefas.business.service.TarefasService;
import com.barrilli.agendadortarefas.infra.enums.StatusNotificacaoEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

// Rotas do job de notificação: fora do JWT, protegidas pelo InternalKeyFilter
@RestController
@RequestMapping("/internal/tarefas")
@RequiredArgsConstructor
public class InternalTarefasController {

    private final TarefasService tarefasService;

    @GetMapping("/pendentes")
    public ResponseEntity<List<TarefasDTO>> buscaTarefasPendentes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim
    ) {
        return ResponseEntity.ok(tarefasService.buscaTarefasAgendadasPorPeriodo(inicio, fim));
    }

    @PatchMapping("/{id}/notificada")
    public ResponseEntity<TarefasDTO> marcaComoNotificada(@PathVariable String id) {
        return ResponseEntity.ok(tarefasService.alteraStatus(StatusNotificacaoEnum.NOTIFICADO, id));
    }
}
