package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarEnderecoRequest;
import br.com.techhelp.model.Endereco;
import br.com.techhelp.service.EnderecoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enderecos")
public class EnderecoController {

    private final EnderecoService enderecoService;

    public EnderecoController(
            EnderecoService enderecoService
    ) {
        this.enderecoService = enderecoService;
    }

    @PostMapping
    public ResponseEntity<Endereco> criar(
            @Valid
            @RequestBody
            CriarEnderecoRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        enderecoService.criar(request)
                );
    }

    @GetMapping("/{idEndereco}")
    public Endereco buscarPorId(
            @PathVariable
            Long idEndereco
    ) {

        return enderecoService
                .buscarPorId(idEndereco);
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<Endereco> listarPorUsuario(
            @PathVariable
            Long idUsuario
    ) {

        return enderecoService
                .listarPorUsuario(idUsuario);
    }

    @PutMapping("/{idEndereco}")
    public Endereco atualizar(
            @PathVariable
            Long idEndereco,

            @Valid
            @RequestBody
            CriarEnderecoRequest request
    ) {

        return enderecoService
                .atualizar(
                        idEndereco,
                        request
                );
    }

    @DeleteMapping("/{idEndereco}")
    public ResponseEntity<Void> excluir(
            @PathVariable
            Long idEndereco
    ) {

        enderecoService.excluir(idEndereco);

        return ResponseEntity
                .noContent()
                .build();
    }
}