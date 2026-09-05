package br.com.techhelp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastrarTecnicoRequest(

        @NotBlank
        String nome,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 6, max = 72)
        String senha,

        String telefone,

        @NotBlank
        @Pattern(regexp = "[0-9]{11}")
        String cpf,

        String descricao,

        @NotNull
        @Min(0)
        Short anosExperiencia

) {
}