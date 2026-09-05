package br.com.techhelp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarNotificacaoRequest(

        @NotNull
        Long idUsuario,

        @NotBlank
        @Size(max = 50)
        String tipo,

        @NotBlank
        @Size(max = 160)
        String titulo,

        @NotBlank
        @Size(max = 500)
        String mensagem,

        @Size(max = 500)
        String linkDestino

) {
}