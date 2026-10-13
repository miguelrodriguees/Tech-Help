package br.com.techhelp.dto;

import java.util.List;

public record SessaoResponse(Long idUsuario, Long idCliente, Long idTecnico,
        String nome, String email, List<String> perfis) {
}
