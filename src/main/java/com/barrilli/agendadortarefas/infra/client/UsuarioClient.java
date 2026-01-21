package com.barrilli.agendadortarefas.infra.client;

import com.barrilli.agendadortarefas.business.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "usuario", url = "${usuario.url}")
public interface UsuarioClient {

    @GetMapping("/v1/usuario/email")
    UsuarioDTO buscaUsuarioPorEmail(@RequestParam String email,
                                    @RequestHeader("Authorization") String token);
}
