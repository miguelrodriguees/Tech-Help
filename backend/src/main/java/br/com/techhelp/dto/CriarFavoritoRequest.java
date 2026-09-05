package br.com.techhelp.dto;

import jakarta.validation.constraints.NotNull;

public record CriarFavoritoRequest(

        @NotNull
        Long idCliente,

        @NotNull
        Long idTecnico

) {
}