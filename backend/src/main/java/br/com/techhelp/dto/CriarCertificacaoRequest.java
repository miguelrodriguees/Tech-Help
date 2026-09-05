package br.com.techhelp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CriarCertificacaoRequest(

        @NotNull
        Long idTecnico,

        @NotBlank
        @Size(max = 150)
        String nome,

        @NotBlank
        @Size(max = 150)
        String instituicao,

        @Size(max = 120)
        String codigoCredencial,

        @Size(max = 500)
        String arquivoUrl,

        @Size(max = 500)
        String urlCredencial,

        LocalDate dataEmissao,

        LocalDate dataValidade

) {
}