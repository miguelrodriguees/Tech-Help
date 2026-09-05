package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarPortfolioRequest;
import br.com.techhelp.model.Portfolio;
import br.com.techhelp.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(
            PortfolioService portfolioService
    ) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    public ResponseEntity<Portfolio> criar(
            @Valid @RequestBody CriarPortfolioRequest request
    ) {

        Portfolio portfolio =
                portfolioService.criar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(portfolio);
    }

    @GetMapping("/tecnico/{idTecnico}")
    public ResponseEntity<List<Portfolio>> listarPorTecnico(
            @PathVariable Long idTecnico
    ) {

        return ResponseEntity.ok(
                portfolioService.listarPorTecnico(idTecnico)
        );
    }

    @DeleteMapping("/{idPortfolio}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long idPortfolio
    ) {

        portfolioService.excluir(idPortfolio);

        return ResponseEntity.noContent().build();
    }
}