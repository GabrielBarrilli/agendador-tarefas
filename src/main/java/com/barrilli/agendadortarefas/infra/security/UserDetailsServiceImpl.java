package com.barrilli.agendadortarefas.infra.security;

import com.barrilli.agendadortarefas.business.UsuarioDTO;
import com.barrilli.agendadortarefas.infra.client.UsuarioClient;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl {

    private final UsuarioClient usuarioClient;

    public UserDetailsServiceImpl(UsuarioClient usuarioClient) {
        this.usuarioClient = usuarioClient;
    }

    public UserDetails carregaDadosUsuario(String email, String token) {
        UsuarioDTO usuarioDTO = usuarioClient.buscaUsuarioPorEmail(email, token);
        return User
                .withUsername(usuarioDTO.getEmail())
                .password(usuarioDTO.getSenha())
                .build();
    }
}
