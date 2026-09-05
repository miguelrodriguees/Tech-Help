package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarPropostaRequest;
import br.com.techhelp.model.Proposta;
import br.com.techhelp.service.PropostaService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/propostas")
public class PropostaController {

    private final PropostaService propostaService;

    public PropostaController(
            PropostaService propostaService
    ) {
        this.propostaService = propostaService;
    }

    @PostMapping
    public ResponseEntity<Proposta> criar(
            @Valid
            @RequestBody
            CriarPropostaRequest dados
    ) {

        Proposta proposta =
                propostaService.criar(dados);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(proposta);
    }

    @GetMapping("/solicitacao/{idSolicitacao}")
    public List<Proposta> listarPorSolicitacao(
            @PathVariable Long idSolicitacao
    ) {

        return propostaService
                .listarPorSolicitacao(
                        idSolicitacao
                );
    }

    @GetMapping("/tecnico/{idTecnico}")
    public List<Proposta> listarPorTecnico(
            @PathVariable Long idTecnico
    ) {

        return propostaService
                .listarPorTecnico(
                        idTecnico
                );
    }
}