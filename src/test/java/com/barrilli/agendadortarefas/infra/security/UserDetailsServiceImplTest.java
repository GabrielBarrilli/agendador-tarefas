package com.barrilli.agendadortarefas.infra.security;

import com.barrilli.agendadortarefas.business.dto.UsuarioDTO;
import com.barrilli.agendadortarefas.infra.client.UsuarioClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserDetailsServiceImplTest {

    @Test
    void carregaUsuarioPeloServicoDeUsuarioSemSenha() {
        UsuarioClient client = mock(UsuarioClient.class);
        when(client.buscaUsuarioPorEmail("ana@t.com", "Bearer tok")).thenReturn(new UsuarioDTO("ana@t.com"));

        var userDetails = new UserDetailsServiceImpl(client).carregaDadosUsuario("ana@t.com", "Bearer tok");

        assertThat(userDetails.getUsername()).isEqualTo("ana@t.com");
        assertThat(userDetails.getPassword()).isEmpty();
    }
}
