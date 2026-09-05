package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarAluguelRequest;
import br.com.techhelp.model.Aluguel;
import br.com.techhelp.service.AluguelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alugueis")
public class AluguelController {

    private final AluguelService aluguelService;

    public AluguelController(
            AluguelService aluguelService
    ) {
        this.aluguelService = aluguelService;
    }

    @PostMapping
    public ResponseEntity<Aluguel> criar(
            @Valid
            @RequestBody
            CriarAluguelRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(aluguelService.criar(request));
    }

    @GetMapping("/{idAluguel}")
    public ResponseEntity<Aluguel> buscar(
            @PathVariable Long idAluguel
    ) {

        return ResponseEntity.ok(
                aluguelService.buscarPorId(idAluguel)
        );
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Aluguel>>
            listarPorUsuario(
                    @PathVariable Long idUsuario
            ) {

        return ResponseEntity.ok(
                aluguelService.listarPorUsuario(idUsuario)
        );
    }

    @PostMapping("/{idAluguel}/retirar")
    public ResponseEntity<Aluguel> retirar(
            @PathVariable Long idAluguel
    ) {

        return ResponseEntity.ok(
                aluguelService.retirar(idAluguel)
        );
    }

    @PostMapping("/{idAluguel}/devolver")
    public ResponseEntity<Aluguel> devolver(
            @PathVariable Long idAluguel
    ) {

        return ResponseEntity.ok(
                aluguelService.devolver(idAluguel)
        );
    }

    @PostMapping("/{idAluguel}/cancelar")
    public ResponseEntity<Aluguel> cancelar(
            @PathVariable Long idAluguel
    ) {

        return ResponseEntity.ok(
                aluguelService.cancelar(idAluguel)
        );
    }
}