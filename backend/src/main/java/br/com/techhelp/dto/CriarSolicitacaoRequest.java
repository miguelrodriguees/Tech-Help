package br.com.techhelp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CriarSolicitacaoRequest(

        @NotNull
        Long idCategoria,

        Long idEndereco,

        @NotBlank
        @Size(max = 160)
        String titulo,

        @NotBlank
        @Size(max = 12000)
        String descricao,

        @NotBlank
        String tipoAtendimento,

        String urgencia,

        @PositiveOrZero
        BigDecimal orcamentoMin,

        @PositiveOrZero
        BigDecimal orcamentoMax

) {
}
