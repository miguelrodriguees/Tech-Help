package br.com.techhelp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ReservarKitRequest(
        @NotNull Long idUsuario,
        @NotNull Long idKit,
        @NotNull @Min(1) Integer quantidade,
        @NotNull LocalDateTime dataPrevistaRetirada,
        @NotNull LocalDateTime dataPrevistaDevolucao) {
}
