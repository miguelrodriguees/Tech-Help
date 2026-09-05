package br.com.techhelp.controller;

import br.com.techhelp.dto.VincularPerfilRequest;
import br.com.techhelp.model.Perfil;
import br.com.techhelp.model.UsuarioPerfil;
import br.com.techhelp.service.PerfilService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/perfis")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(
            PerfilService perfilService
    ) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public List<Perfil> listarPerfis() {

        return perfilService.listarPerfis();
    }

    @PostMapping("/vinculos")
    public ResponseEntity<UsuarioPerfil> vincular(
            @Valid
            @RequestBody
            VincularPerfilRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        perfilService.vincular(
                                request
                        )
                );
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<UsuarioPerfil> listarPorUsuario(
            @PathVariable
            Long idUsuario
    ) {

        return perfilService
                .listarPorUsuario(idUsuario);
    }

    @DeleteMapping(
            "/usuario/{idUsuario}/perfil/{idPerfil}"
    )
    public ResponseEntity<Void> remover(
            @PathVariable
            Long idUsuario,

            @PathVariable
            Long idPerfil
    ) {

        perfilService.remover(
                idUsuario,
                idPerfil
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}