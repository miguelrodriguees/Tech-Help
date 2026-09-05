package br.com.techhelp.controller;

import br.com.techhelp.dto.EnviarMensagemRequest;
import br.com.techhelp.model.Mensagem;
import br.com.techhelp.service.MensagemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversas")
public class MensagemController {

    private final MensagemService mensagemService;

    public MensagemController(
            MensagemService mensagemService
    ) {
        this.mensagemService = mensagemService;
    }

    @PostMapping("/{idConversa}/mensagens")
    public ResponseEntity<Mensagem> enviar(
            @PathVariable Long idConversa,
            @Valid
            @RequestBody EnviarMensagemRequest request
    ) {

        Mensagem mensagem =
                mensagemService.enviar(
                        idConversa,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mensagem);
    }

    @GetMapping("/{idConversa}/mensagens")
    public ResponseEntity<List<Mensagem>> listar(
            @PathVariable Long idConversa,
            @RequestParam Long idUsuario
    ) {

        return ResponseEntity.ok(
                mensagemService.listar(
                        idConversa,
                        idUsuario
                )
        );
    }
}