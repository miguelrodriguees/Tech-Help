package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarSolicitacaoRequest;
import br.com.techhelp.model.Solicitacao;
import br.com.techhelp.service.SolicitacaoService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    public SolicitacaoController(
            SolicitacaoService solicitacaoService
    ) {
        this.solicitacaoService = solicitacaoService;
    }

    @PostMapping
    public ResponseEntity<Solicitacao> criar(
            @Valid
            @RequestBody
            CriarSolicitacaoRequest dados
    ) {

        Solicitacao solicitacao =
                solicitacaoService.criar(dados);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(solicitacao);
    }

    @GetMapping("/abertas")
    public List<Solicitacao> listarAbertas() {

        return solicitacaoService
                .listarAbertas();
    }

    @GetMapping("/cliente/{idCliente}")
    public List<Solicitacao> listarPorCliente(
            @PathVariable Long idCliente
    ) {

        return solicitacaoService
                .listarPorCliente(idCliente);
    }

    @GetMapping("/{id}")
    public Solicitacao buscar(
            @PathVariable Long id
    ) {

        return solicitacaoService
                .buscarPorId(id);
    }
}