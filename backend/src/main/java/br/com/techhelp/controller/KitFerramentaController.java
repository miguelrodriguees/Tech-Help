package br.com.techhelp.controller;

import br.com.techhelp.dto.AdicionarFerramentaKitRequest;
import br.com.techhelp.model.KitFerramenta;
import br.com.techhelp.service.KitFerramentaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kits")
public class KitFerramentaController {

    private final KitFerramentaService kitFerramentaService;

    public KitFerramentaController(
            KitFerramentaService kitFerramentaService
    ) {
        this.kitFerramentaService = kitFerramentaService;
    }

    @PostMapping("/{idKit}/ferramentas")
    public ResponseEntity<KitFerramenta> adicionar(
            @PathVariable Long idKit,
            @Valid
            @RequestBody
            AdicionarFerramentaKitRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        kitFerramentaService.adicionar(
                                idKit,
                                request
                        )
                );
    }

    @GetMapping("/{idKit}/ferramentas")
    public ResponseEntity<List<KitFerramenta>> listar(
            @PathVariable Long idKit
    ) {

        return ResponseEntity.ok(
                kitFerramentaService.listar(idKit)
        );
    }

    @DeleteMapping(
            "/{idKit}/ferramentas/{idFerramenta}"
    )
    public ResponseEntity<Void> remover(
            @PathVariable Long idKit,
            @PathVariable Long idFerramenta
    ) {

        kitFerramentaService.remover(
                idKit,
                idFerramenta
        );

        return ResponseEntity.noContent().build();
    }
}