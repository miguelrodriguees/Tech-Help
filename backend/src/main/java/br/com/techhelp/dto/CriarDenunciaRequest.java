package br.com.techhelp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarDenunciaRequest(

        @NotNull
        Long idDenunciante,

        @NotNull
        Long idUsuarioDenunciado,

        Long idServico,

        @NotBlank
        String motivo,

        @NotBlank
        String descricao

) {
}