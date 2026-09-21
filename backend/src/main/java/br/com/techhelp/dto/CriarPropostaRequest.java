package br.com.techhelp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CriarPropostaRequest(

        @NotNull
        Long idSolicitacao,

        @NotNull
        @Positive
        @jakarta.validation.constraints.Digits(integer = 10, fraction = 2)
        BigDecimal valor,

        @jakarta.validation.constraints.Size(max = 3000)
        String mensagem,

        @PositiveOrZero
        Short prazoEstimadoDias,

        @jakarta.validation.constraints.FutureOrPresent
        LocalDate dataDisponivel

) {
}