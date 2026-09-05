package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarFavoritoRequest;
import br.com.techhelp.model.Favorito;
import br.com.techhelp.service.FavoritoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(
            FavoritoService favoritoService
    ) {
        this.favoritoService = favoritoService;
    }

    @PostMapping
    public ResponseEntity<Favorito> adicionar(
            @Valid @RequestBody CriarFavoritoRequest request
    ) {

        Favorito favorito =
                favoritoService.adicionar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(favorito);
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<Favorito>> listar(
            @PathVariable Long idCliente
    ) {

        return ResponseEntity.ok(
                favoritoService.listarPorCliente(idCliente)
        );
    }

    @DeleteMapping(
            "/cliente/{idCliente}/tecnico/{idTecnico}"
    )
    public ResponseEntity<Void> remover(
            @PathVariable Long idCliente,
            @PathVariable Long idTecnico
    ) {

        favoritoService.remover(
                idCliente,
                idTecnico
        );

        return ResponseEntity.noContent().build();
    }
}