package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarSolicitacaoAnexoRequest;
import br.com.techhelp.model.SolicitacaoAnexo;
import br.com.techhelp.service.SolicitacaoAnexoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/solicitacoes")
public class SolicitacaoAnexoController {

    private final SolicitacaoAnexoService anexoService;

    public SolicitacaoAnexoController(
            SolicitacaoAnexoService anexoService
    ) {
        this.anexoService = anexoService;
    }

    @PostMapping("/{idSolicitacao}/anexos")
    public ResponseEntity<SolicitacaoAnexo> adicionar(
            @PathVariable Long idSolicitacao,

            @Valid
            @RequestBody
            CriarSolicitacaoAnexoRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        anexoService.adicionar(
                                idSolicitacao,
                                request
                        )
                );
    }

    @GetMapping("/{idSolicitacao}/anexos")
    public List<SolicitacaoAnexo> listar(
            @PathVariable Long idSolicitacao
    ) {

        return anexoService
                .listar(idSolicitacao);
    }

    @GetMapping(
            "/{idSolicitacao}/anexos/{idAnexo}"
    )
    public SolicitacaoAnexo buscar(
            @PathVariable Long idSolicitacao,
            @PathVariable Long idAnexo
    ) {

        SolicitacaoAnexo anexo =
                anexoService.buscarPorId(idAnexo);

        if (!anexo.getIdSolicitacao()
                .equals(idSolicitacao)) {

            throw new IllegalArgumentException(
                    "Este anexo não pertence à solicitação informada"
            );
        }

        return anexo;
    }

    @DeleteMapping(
            "/{idSolicitacao}/anexos/{idAnexo}"
    )
    public ResponseEntity<Void> excluir(
            @PathVariable Long idSolicitacao,
            @PathVariable Long idAnexo
    ) {

        anexoService.excluir(
                idSolicitacao,
                idAnexo
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}