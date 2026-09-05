package br.com.techhelp.controller;

import br.com.techhelp.dto.AdicionarItemAluguelRequest;
import br.com.techhelp.model.AluguelItem;
import br.com.techhelp.service.AluguelItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alugueis")
public class AluguelItemController {

    private final AluguelItemService aluguelItemService;

    public AluguelItemController(
            AluguelItemService aluguelItemService
    ) {
        this.aluguelItemService = aluguelItemService;
    }

    @PostMapping("/{idAluguel}/itens")
    public ResponseEntity<AluguelItem> adicionar(
            @PathVariable Long idAluguel,
            @Valid
            @RequestBody
            AdicionarItemAluguelRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        aluguelItemService.adicionar(
                                idAluguel,
                                request
                        )
                );
    }

    @GetMapping("/{idAluguel}/itens")
    public ResponseEntity<List<AluguelItem>> listar(
            @PathVariable Long idAluguel
    ) {

        return ResponseEntity.ok(
                aluguelItemService.listar(idAluguel)
        );
    }

    @DeleteMapping(
            "/{idAluguel}/itens/{idAluguelItem}"
    )
    public ResponseEntity<Void> remover(
            @PathVariable Long idAluguel,
            @PathVariable Long idAluguelItem
    ) {

        aluguelItemService.remover(
                idAluguel,
                idAluguelItem
        );

        return ResponseEntity.noContent().build();
    }
}