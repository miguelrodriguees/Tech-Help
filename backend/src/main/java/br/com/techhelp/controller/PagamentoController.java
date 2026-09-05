package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarPagamentoRequest;
import br.com.techhelp.model.Pagamento;
import br.com.techhelp.service.PagamentoService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(
            PagamentoService pagamentoService
    ) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    public ResponseEntity<Pagamento> criar(
            @Valid
            @RequestBody
            CriarPagamentoRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        pagamentoService.criar(
                                request
                        )
                );
    }

    @GetMapping("/{idPagamento}")
    public Pagamento buscarPorId(
            @PathVariable
            Long idPagamento
    ) {

        return pagamentoService
                .buscarPorId(idPagamento);
    }

    @GetMapping("/servico/{idServico}")
    public List<Pagamento> listarPorServico(
            @PathVariable
            Long idServico
    ) {

        return pagamentoService
                .listarPorServico(idServico);
    }

    @GetMapping("/aluguel/{idAluguel}")
    public List<Pagamento> listarPorAluguel(
            @PathVariable
            Long idAluguel
    ) {

        return pagamentoService
                .listarPorAluguel(idAluguel);
    }

    @PostMapping("/{idPagamento}/pagar")
    public Pagamento pagar(
            @PathVariable
            Long idPagamento
    ) {

        return pagamentoService
                .pagar(idPagamento);
    }

    @PostMapping("/{idPagamento}/cancelar")
    public Pagamento cancelar(
            @PathVariable
            Long idPagamento
    ) {

        return pagamentoService
                .cancelar(idPagamento);
    }
}