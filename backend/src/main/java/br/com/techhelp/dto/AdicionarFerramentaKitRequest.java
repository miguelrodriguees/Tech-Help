package br.com.techhelp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdicionarFerramentaKitRequest(

        @NotNull
        Long idFerramenta,

        @NotNull
        @Min(1)
        Integer quantidade

) {
}