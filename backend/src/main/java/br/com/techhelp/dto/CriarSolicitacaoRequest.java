package br.com.techhelp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import jakarta.validation.constraints.Digits;

public record CriarSolicitacaoRequest(

        @NotNull
        Long idCliente,

        @NotNull
        Long idCategoria,

        Long idEndereco,

        @NotBlank
        @Size(max = 160)
        String titulo,

        @NotBlank
        String descricao,

        @NotBlank
        String tipoAtendimento,

        String urgencia,

        @PositiveOrZero
        @Digits(integer = 10, fraction = 2)
        BigDecimal orcamentoMin,

        @PositiveOrZero
        @Digits(integer = 10, fraction = 2)
        BigDecimal orcamentoMax

) {
}
