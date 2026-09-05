package br.com.techhelp.dto;

public record CadastroResponse(

        Long idUsuario,
        Long idCliente,
        Long idTecnico,
        String nome,
        String email,
        String perfil

) {
}