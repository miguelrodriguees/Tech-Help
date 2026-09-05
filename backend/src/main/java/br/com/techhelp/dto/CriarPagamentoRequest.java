package br.com.techhelp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CriarPagamentoRequest(

        Long idServico,

        Long idAluguel,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal valor,

        @NotBlank
        String formaPagamento

) {
}