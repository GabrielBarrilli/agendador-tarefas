package com.barrilli.agendadortarefas.business;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioDTO {
    private String email;
    private String senha;
}
