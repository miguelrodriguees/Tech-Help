package br.com.techhelp.dto;

import jakarta.validation.constraints.NotNull;

public record VincularPerfilRequest(

        @NotNull
        Long idUsuario,

        @NotNull
        Long idPerfil

) {
}