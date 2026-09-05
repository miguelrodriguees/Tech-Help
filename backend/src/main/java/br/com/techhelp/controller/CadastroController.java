package br.com.techhelp.controller;

import br.com.techhelp.dto.CadastrarClienteRequest;
import br.com.techhelp.dto.CadastrarTecnicoRequest;
import br.com.techhelp.dto.CadastroResponse;
import br.com.techhelp.service.CadastroService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cadastro")
public class CadastroController {

    private final CadastroService cadastroService;

    public CadastroController(
            CadastroService cadastroService
    ) {
        this.cadastroService = cadastroService;
    }

    @PostMapping("/cliente")
    public ResponseEntity<CadastroResponse>
    cadastrarCliente(
            @Valid
            @RequestBody
            CadastrarClienteRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        cadastroService
                                .cadastrarCliente(request)
                );
    }

    @PostMapping("/tecnico")
    public ResponseEntity<CadastroResponse>
    cadastrarTecnico(
            @Valid
            @RequestBody
            CadastrarTecnicoRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        cadastroService
                                .cadastrarTecnico(request)
                );
    }
}