package br.com.techhelp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdicionarItemAluguelRequest(

        @NotNull
        Long idKit,

        @NotNull
        @Min(1)
        Integer quantidade

) {
}