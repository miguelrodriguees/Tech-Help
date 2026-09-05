package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarNotificacaoRequest;
import br.com.techhelp.model.Notificacao;
import br.com.techhelp.service.NotificacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(
            NotificacaoService notificacaoService
    ) {
        this.notificacaoService = notificacaoService;
    }

    @PostMapping
    public ResponseEntity<Notificacao> criar(
            @Valid @RequestBody CriarNotificacaoRequest request
    ) {

        Notificacao notificacao =
                notificacaoService.criar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificacao);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Notificacao>> listar(
            @PathVariable Long idUsuario
    ) {

        return ResponseEntity.ok(
                notificacaoService.listarPorUsuario(idUsuario)
        );
    }

    @GetMapping("/usuario/{idUsuario}/nao-lidas")
    public ResponseEntity<List<Notificacao>> listarNaoLidas(
            @PathVariable Long idUsuario
    ) {

        return ResponseEntity.ok(
                notificacaoService.listarNaoLidas(idUsuario)
        );
    }

    @GetMapping("/usuario/{idUsuario}/quantidade-nao-lidas")
    public ResponseEntity<Map<String, Long>> contarNaoLidas(
            @PathVariable Long idUsuario
    ) {

        long quantidade =
                notificacaoService.contarNaoLidas(idUsuario);

        return ResponseEntity.ok(
                Map.of("quantidade", quantidade)
        );
    }

    @PostMapping("/{idNotificacao}/ler")
    public ResponseEntity<Notificacao> marcarComoLida(
            @PathVariable Long idNotificacao
    ) {

        return ResponseEntity.ok(
                notificacaoService.marcarComoLida(idNotificacao)
        );
    }
}