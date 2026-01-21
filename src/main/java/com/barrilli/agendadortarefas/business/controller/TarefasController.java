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

@RestController
@RequestMapping("/v1/tarefas")
@RequiredArgsConstructor
public class TarefasController {

    private final TarefasService tarefasService;

    @PostMapping
    public ResponseEntity<TarefasDTO> gravarTarefa(@RequestHeader("Authorization") String token,
                                                   @RequestBody TarefasDTO tarefasDTO) {
        return ResponseEntity.ok(tarefasService.gravarTarefa(token, tarefasDTO));
    }

    @GetMapping("/eventos")
    public ResponseEntity<List<TarefasDTO>> buscaListaTarefasPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFinal
    ) {
        return ResponseEntity.ok(tarefasService.buscaTarefasAgendadasPorPeriodo(dataInicial, dataFinal));
    }

    @GetMapping("/email")
    public ResponseEntity<List<TarefasDTO>> buscaListaTarefasPorEmail(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(tarefasService.buscaTarefasPorEmail(token));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleterTarefaPorId(@RequestParam String id) {
        tarefasService.deletarTarefaPorId(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    public ResponseEntity<TarefasDTO> alteraStatusNotificacao(
            @RequestParam("status") StatusNotificacaoEnum statusNotificacaoEnum,
            @RequestParam("id") String id) {
        return ResponseEntity.ok(tarefasService.alteraStatus(statusNotificacaoEnum, id));
    }

    @PutMapping
    public ResponseEntity<TarefasDTO> updateTarefas(@RequestBody TarefasDTO tarefasDTO, @RequestParam("id") String id) {
        return ResponseEntity.ok(tarefasService.updateTarefa(tarefasDTO, id));
    }

}
