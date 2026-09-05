package br.com.techhelp.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CriarAluguelRequest(

        @NotNull
        Long idUsuario,

        @NotNull
        LocalDateTime dataPrevistaRetirada,

        @NotNull
        LocalDateTime dataPrevistaDevolucao

) {
}