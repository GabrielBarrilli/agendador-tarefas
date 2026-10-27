package com.barrilli.agendadortarefas.business.controller;

import com.barrilli.agendadortarefas.business.dto.TarefasDTO;
import com.barrilli.agendadortarefas.business.service.TarefasService;
import com.barrilli.agendadortarefas.infra.enums.StatusNotificacaoEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/email")
    public ResponseEntity<List<TarefasDTO>> buscaListaTarefasPorEmail(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(tarefasService.buscaTarefasPorEmail(token));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleterTarefaPorId(@RequestHeader("Authorization") String token,
                                                   @RequestParam String id) {
        tarefasService.deletarTarefaPorId(token, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    public ResponseEntity<TarefasDTO> alteraStatusNotificacao(
            @RequestHeader("Authorization") String token,
            @RequestParam("status") StatusNotificacaoEnum statusNotificacaoEnum,
            @RequestParam("id") String id) {
        return ResponseEntity.ok(tarefasService.alteraStatus(token, statusNotificacaoEnum, id));
    }

    @PutMapping
    public ResponseEntity<TarefasDTO> updateTarefas(@RequestHeader("Authorization") String token,
                                                    @RequestBody TarefasDTO tarefasDTO,
                                                    @RequestParam("id") String id) {
        return ResponseEntity.ok(tarefasService.updateTarefa(token, tarefasDTO, id));
    }

}
