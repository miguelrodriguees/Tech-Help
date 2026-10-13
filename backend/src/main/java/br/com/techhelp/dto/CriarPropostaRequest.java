package br.com.techhelp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import jakarta.validation.constraints.Digits;
import java.time.LocalDate;

public record CriarPropostaRequest(

        @NotNull
        Long idSolicitacao,

        @NotNull
        Long idTecnico,

        @NotNull
        @Positive
        @Digits(integer = 10, fraction = 2)
        BigDecimal valor,

        String mensagem,

        @PositiveOrZero
        Short prazoEstimadoDias,

        LocalDate dataDisponivel

) {
}