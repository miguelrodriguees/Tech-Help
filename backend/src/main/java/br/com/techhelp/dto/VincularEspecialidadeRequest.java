package br.com.techhelp.dto;

import jakarta.validation.constraints.NotNull;

public record VincularEspecialidadeRequest(

        @NotNull
        Long idTecnico,

        @NotNull
        Long idEspecialidade

) {
}