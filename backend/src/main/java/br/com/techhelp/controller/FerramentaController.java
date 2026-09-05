package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarFerramentaRequest;
import br.com.techhelp.model.Ferramenta;
import br.com.techhelp.service.FerramentaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ferramentas")
public class FerramentaController {

    private final FerramentaService ferramentaService;

    public FerramentaController(
            FerramentaService ferramentaService
    ) {
        this.ferramentaService = ferramentaService;
    }

    @PostMapping
    public ResponseEntity<Ferramenta> criar(
            @Valid
            @RequestBody
            CriarFerramentaRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ferramentaService.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<Ferramenta>> listar() {

        return ResponseEntity.ok(
                ferramentaService.listar()
        );
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<Ferramenta>>
            listarDisponiveis() {

        return ResponseEntity.ok(
                ferramentaService.listarDisponiveis()
        );
    }

    @GetMapping("/{idFerramenta}")
    public ResponseEntity<Ferramenta> buscar(
            @PathVariable Long idFerramenta
    ) {

        return ResponseEntity.ok(
                ferramentaService.buscarPorId(idFerramenta)
        );
    }
}