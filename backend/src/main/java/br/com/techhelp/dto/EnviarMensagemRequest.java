package br.com.techhelp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EnviarMensagemRequest(

        @NotNull
        Long idUsuario,

        @Size(max = 20)
        String tipo,

        String texto,

        @Size(max = 500)
        String arquivoUrl

) {
}