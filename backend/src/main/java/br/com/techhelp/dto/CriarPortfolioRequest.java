package br.com.techhelp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarPortfolioRequest(

        @NotNull
        Long idTecnico,

        @NotBlank
        @Size(max = 150)
        String titulo,

        String descricao,

        @Size(max = 500)
        String imagemUrl,

        @Size(max = 500)
        String linkProjeto

) {
}