package br.com.techhelp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

public record AvaliarAtendimentoRequest(
        @NotNull @Min(1) @Max(5) @Digits(integer = 1, fraction = 0) BigDecimal nota,
        @Size(max = 3000) String comentario
) {}
