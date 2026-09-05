package br.com.techhelp.controller;

import br.com.techhelp.model.Tecnico;
import br.com.techhelp.service.TecnicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tecnicos")
public class TecnicoController {

    private final TecnicoService tecnicoService;

    public TecnicoController(TecnicoService tecnicoService) {
        this.tecnicoService = tecnicoService;
    }

    @GetMapping
    public ResponseEntity<List<Tecnico>> listarTodos() {
        return ResponseEntity.ok(
                tecnicoService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tecnico> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                tecnicoService.buscarPorId(id)
        );
    }
}