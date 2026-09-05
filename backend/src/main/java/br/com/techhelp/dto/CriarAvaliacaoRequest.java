package br.com.techhelp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CriarAvaliacaoRequest(

        @NotNull
        Long idServico,

        @NotNull
        Long idAvaliador,

        @NotNull
        Long idAvaliado,

        @NotNull
        @Min(1)
        @Max(5)
        Integer nota,

        String comentario

) {
}