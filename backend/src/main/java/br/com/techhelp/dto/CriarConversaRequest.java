package br.com.techhelp.dto;

import jakarta.validation.constraints.NotNull;

public record CriarConversaRequest(

        @NotNull
        Long idSolicitacao,

        @NotNull
        Long idUsuario1,

        @NotNull
        Long idUsuario2

) {
}