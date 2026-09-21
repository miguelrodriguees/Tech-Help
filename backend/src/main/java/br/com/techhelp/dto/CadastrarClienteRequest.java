package br.com.techhelp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastrarClienteRequest(

        @NotBlank
        @Size(max = 120)
        String nome,

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        @Size(min = 6, max = 72)
        String senha,

        @Size(max = 20)
        String telefone,

        @NotBlank
        @Pattern(regexp = "[0-9]{11}")
        String cpf

) {
}