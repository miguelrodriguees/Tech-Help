package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarKitRequest;
import br.com.techhelp.model.Kit;
import br.com.techhelp.service.KitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kits")
public class KitController {

    private final KitService kitService;

    public KitController(
            KitService kitService
    ) {
        this.kitService = kitService;
    }

    @PostMapping
    public ResponseEntity<Kit> criar(
            @Valid
            @RequestBody CriarKitRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(kitService.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<Kit>> listar() {

        return ResponseEntity.ok(
                kitService.listar()
        );
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<Kit>>
            listarDisponiveis() {

        return ResponseEntity.ok(
                kitService.listarDisponiveis()
        );
    }

    @GetMapping("/{idKit}")
    public ResponseEntity<Kit> buscar(
            @PathVariable Long idKit
    ) {

        return ResponseEntity.ok(
                kitService.buscarPorId(idKit)
        );
    }
}