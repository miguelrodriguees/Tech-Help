package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarConversaRequest;
import br.com.techhelp.model.Conversa;
import br.com.techhelp.service.ConversaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversas")
public class ConversaController {

    private final ConversaService conversaService;

    public ConversaController(
            ConversaService conversaService
    ) {
        this.conversaService = conversaService;
    }

    @PostMapping
    public ResponseEntity<Conversa> criar(
            @Valid
            @RequestBody
            CriarConversaRequest request
    ) {

        Conversa conversa =
                conversaService.criar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(conversa);
    }

    @GetMapping("/{idConversa}")
    public ResponseEntity<Conversa> buscar(
            @PathVariable Long idConversa
    ) {

        return ResponseEntity.ok(
                conversaService.buscarPorId(idConversa)
        );
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Conversa>>
            listarPorUsuario(
                    @PathVariable Long idUsuario
            ) {

        return ResponseEntity.ok(
                conversaService
                        .listarPorUsuario(idUsuario)
        );
    }
}