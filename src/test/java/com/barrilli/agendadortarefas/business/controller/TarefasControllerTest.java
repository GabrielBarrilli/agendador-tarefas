package com.barrilli.agendadortarefas.business.controller;

import com.barrilli.agendadortarefas.business.dto.TarefasDTO;
import com.barrilli.agendadortarefas.business.service.TarefasService;
import com.barrilli.agendadortarefas.infra.enums.StatusNotificacaoEnum;
import com.barrilli.agendadortarefas.infra.exception.ResourceNotFoundException;
import com.barrilli.agendadortarefas.infra.security.InternalKeyFilter;
import com.barrilli.agendadortarefas.infra.security.JwtUtil;
import com.barrilli.agendadortarefas.infra.security.SecurityConfig;
import com.barrilli.agendadortarefas.infra.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TarefasController.class, InternalTarefasController.class},
        properties = "internal.api-key=chave-de-teste")
@Import({SecurityConfig.class, InternalKeyFilter.class})
class TarefasControllerTest {

    private static final String AUTH = "Bearer tok";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TarefasService tarefasService;

    @MockitoBean
    JwtUtil jwtUtil;

    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void tokenValido() {
        when(jwtUtil.extractUsername("tok")).thenReturn("ana@t.com");
        when(jwtUtil.validateToken("tok", "ana@t.com")).thenReturn(true);
        when(userDetailsService.carregaDadosUsuario("ana@t.com", AUTH))
                .thenReturn(User.withUsername("ana@t.com").password("").build());
    }

    @Test
    void semToken_eNegado() throws Exception {
        mockMvc.perform(get("/v1/tarefas/email"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(tarefasService);
    }

    @Test
    void comToken_listaAsTarefasDoUsuario() throws Exception {
        when(tarefasService.buscaTarefasPorEmail(AUTH))
                .thenReturn(List.of(TarefasDTO.builder().id("t1").emailUsuario("ana@t.com").build()));

        mockMvc.perform(get("/v1/tarefas/email").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("t1"));
    }

    @Test
    void comToken_criaTarefa() throws Exception {
        when(tarefasService.gravarTarefa(eq(AUTH), any())).thenReturn(TarefasDTO.builder().id("t1").build());

        mockMvc.perform(post("/v1/tarefas").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomeTarefa\":\"Ligar\",\"dataEvento\":\"10-01-2026 15:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("t1"));
    }

    @Test
    void deleteDeTarefaPropria_retorna204() throws Exception {
        mockMvc.perform(delete("/v1/tarefas").param("id", "t1").header("Authorization", AUTH))
                .andExpect(status().isNoContent());
        verify(tarefasService).deletarTarefaPorId(AUTH, "t1");
    }

    @Test
    void tarefaDeOutroUsuario_retorna404() throws Exception {
        doThrow(new ResourceNotFoundException("Tarefa não encontrada: t1"))
                .when(tarefasService).deletarTarefaPorId(AUTH, "t1");

        mockMvc.perform(delete("/v1/tarefas").param("id", "t1").header("Authorization", AUTH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tarefa não encontrada: t1"));
    }

    @Test
    void alteraStatus_retorna200() throws Exception {
        when(tarefasService.alteraStatus(AUTH, StatusNotificacaoEnum.CANCELADO, "t1"))
                .thenReturn(TarefasDTO.builder().id("t1").statusNotificacao(StatusNotificacaoEnum.CANCELADO).build());

        mockMvc.perform(patch("/v1/tarefas").param("status", "CANCELADO").param("id", "t1").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusNotificacao").value("CANCELADO"));
    }

    @Test
    void atualizaTarefa_retorna200() throws Exception {
        when(tarefasService.updateTarefa(eq(AUTH), any(), eq("t1"))).thenReturn(TarefasDTO.builder().id("t1").build());

        mockMvc.perform(put("/v1/tarefas").param("id", "t1").header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nomeTarefa\":\"Novo\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void erroInesperado_retorna500Generico() throws Exception {
        when(tarefasService.buscaTarefasPorEmail(AUTH)).thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(get("/v1/tarefas/email").header("Authorization", AUTH))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado. Tente novamente mais tarde."));
    }

    @Test
    void rotaInterna_semChave_retorna401() throws Exception {
        mockMvc.perform(get("/internal/tarefas/pendentes")
                        .param("inicio", "2026-01-10T15:00:00").param("fim", "2026-01-10T15:05:00"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(tarefasService);
    }

    @Test
    void rotaInterna_comChaveErrada_retorna401() throws Exception {
        mockMvc.perform(get("/internal/tarefas/pendentes").header("X-Internal-Key", "errada")
                        .param("inicio", "2026-01-10T15:00:00").param("fim", "2026-01-10T15:05:00"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaInterna_comChave_buscaPendentes() throws Exception {
        when(tarefasService.buscaTarefasAgendadasPorPeriodo(any(), any()))
                .thenReturn(List.of(TarefasDTO.builder().id("t1").build()));

        mockMvc.perform(get("/internal/tarefas/pendentes").header("X-Internal-Key", "chave-de-teste")
                        .param("inicio", "2026-01-10T15:00:00").param("fim", "2026-01-10T15:05:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("t1"));
    }

    @Test
    void rotaInterna_comChave_marcaComoNotificada() throws Exception {
        when(tarefasService.alteraStatus(StatusNotificacaoEnum.NOTIFICADO, "t1"))
                .thenReturn(TarefasDTO.builder().id("t1").statusNotificacao(StatusNotificacaoEnum.NOTIFICADO).build());

        mockMvc.perform(patch("/internal/tarefas/t1/notificada").header("X-Internal-Key", "chave-de-teste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusNotificacao").value("NOTIFICADO"));
    }
}
