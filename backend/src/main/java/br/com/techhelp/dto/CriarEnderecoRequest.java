package br.com.techhelp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CriarEnderecoRequest(

        @NotNull
        Long idUsuario,

        String apelido,

        @NotBlank
        @Pattern(regexp = "[0-9]{8}")
        String cep,

        @NotBlank
        String logradouro,

        @NotBlank
        String numero,

        String complemento,

        @NotBlank
        String bairro,

        @NotBlank
        String cidade,

        @NotBlank
        @Pattern(regexp = "[A-Z]{2}")
        String estado,

        Boolean principal

) {
}