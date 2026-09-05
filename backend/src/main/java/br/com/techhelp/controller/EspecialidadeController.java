package br.com.techhelp.controller;

import br.com.techhelp.model.Especialidade;
import br.com.techhelp.service.EspecialidadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/especialidades")
public class EspecialidadeController {

    private final EspecialidadeService especialidadeService;

    public EspecialidadeController(
            EspecialidadeService especialidadeService
    ) {
        this.especialidadeService = especialidadeService;
    }

    @GetMapping
    public ResponseEntity<List<Especialidade>> listarAtivas() {
        return ResponseEntity.ok(
                especialidadeService.listarAtivas()
        );
    }

    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<Especialidade>> listarPorCategoria(
            @PathVariable Long idCategoria
    ) {
        return ResponseEntity.ok(
                especialidadeService.listarPorCategoria(idCategoria)
        );
    }
}