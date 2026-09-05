package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarCertificacaoRequest;
import br.com.techhelp.model.Certificacao;
import br.com.techhelp.service.CertificacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/certificacoes")
public class CertificacaoController {

    private final CertificacaoService certificacaoService;

    public CertificacaoController(
            CertificacaoService certificacaoService
    ) {
        this.certificacaoService = certificacaoService;
    }

    @PostMapping
    public ResponseEntity<Certificacao> criar(
            @Valid @RequestBody CriarCertificacaoRequest request
    ) {

        Certificacao certificacao =
                certificacaoService.criar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(certificacao);
    }

    @GetMapping("/tecnico/{idTecnico}")
    public ResponseEntity<List<Certificacao>> listarPorTecnico(
            @PathVariable Long idTecnico
    ) {

        return ResponseEntity.ok(
                certificacaoService
                        .listarPorTecnico(idTecnico)
        );
    }

    @DeleteMapping("/{idCertificacao}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long idCertificacao
    ) {

        certificacaoService.excluir(idCertificacao);

        return ResponseEntity.noContent().build();
    }
}