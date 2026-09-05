package br.com.techhelp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CriarSolicitacaoAnexoRequest(

        @NotBlank
        String nomeOriginal,

        @NotBlank
        String arquivoUrl,

        @NotBlank
        String tipoArquivo,

        @PositiveOrZero
        Long tamanhoBytes

) {
}