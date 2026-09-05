package br.com.techhelp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CriarFerramentaRequest(

        @NotBlank
        @Size(max = 140)
        String nome,

        String descricao,

        @NotNull
        @Min(0)
        Integer quantidadeTotal,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal valorDiaria

) {
}