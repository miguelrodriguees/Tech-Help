package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarAvaliacaoRequest;
import br.com.techhelp.model.Avaliacao;
import br.com.techhelp.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(
            AvaliacaoService avaliacaoService
    ) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    public ResponseEntity<Avaliacao> criar(
            @Valid @RequestBody CriarAvaliacaoRequest request
    ) {

        Avaliacao avaliacao =
                avaliacaoService.criar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(avaliacao);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Avaliacao>> listarPorUsuario(
            @PathVariable Long idUsuario
    ) {

        return ResponseEntity.ok(
                avaliacaoService.listarPorUsuario(idUsuario)
        );
    }

    @GetMapping("/servico/{idServico}")
    public ResponseEntity<List<Avaliacao>> listarPorServico(
            @PathVariable Long idServico
    ) {

        return ResponseEntity.ok(
                avaliacaoService.listarPorServico(idServico)
        );
    }
}