package br.com.techhelp.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AtualizarKitRequest(
        @NotBlank @Size(max = 140) String nome,
        String descricao,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal valorDiaria,
        @NotNull @Pattern(regexp = "DISPONIVEL|INATIVO") String status) {}
