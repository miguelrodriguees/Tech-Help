package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarSolicitacaoRequest;
import br.com.techhelp.model.Solicitacao;
import br.com.techhelp.service.SolicitacaoService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;
    private final br.com.techhelp.service.ClienteAutenticado clienteAutenticado;

    public SolicitacaoController(
            SolicitacaoService solicitacaoService,
            br.com.techhelp.service.ClienteAutenticado clienteAutenticado
    ) {
        this.solicitacaoService = solicitacaoService;
        this.clienteAutenticado = clienteAutenticado;
    }

    @PostMapping
    public ResponseEntity<Solicitacao> criar(
            @Valid
            @RequestBody
            CriarSolicitacaoRequest dados,
            java.security.Principal principal,
            @RequestHeader("Idempotency-Key") String chave,
            jakarta.servlet.http.HttpSession session
    ) {

        if (!chave.matches("[a-zA-Z0-9-]{16,80}")) throw new IllegalArgumentException("Chave inválida");
        Long idCliente = clienteAutenticado.id(principal);
        synchronized (session) {
            String atributo = "publicacao:" + idCliente + ":" + chave;
            var anterior = (Publicacao) session.getAttribute(atributo);
            if (anterior != null) {
                if (!anterior.dados().equals(dados)) throw new IllegalArgumentException("Use outra chave para outro pedido");
                return ResponseEntity.ok(anterior.solicitacao());
            }
            var solicitacao = solicitacaoService.criar(dados, idCliente);
            session.setAttribute(atributo, new Publicacao(dados, solicitacao));
            return ResponseEntity.status(HttpStatus.CREATED).body(solicitacao);
        }
    }
    private record Publicacao(CriarSolicitacaoRequest dados, Solicitacao solicitacao) {}

    @GetMapping("/minhas")
    public List<Solicitacao> minhas(java.security.Principal principal) {
        return solicitacaoService.listarPorCliente(clienteAutenticado.id(principal));
    }

    @GetMapping("/abertas")
    public List<Solicitacao> listarAbertas() {

        return solicitacaoService
                .listarAbertas();
    }

    @GetMapping("/cliente/{idCliente}")
    public List<Solicitacao> listarPorCliente(
            @PathVariable Long idCliente
    ) {

        return solicitacaoService
                .listarPorCliente(idCliente);
    }

    @GetMapping("/{id}")
    public Solicitacao buscar(
            @PathVariable Long id
    ) {

        return solicitacaoService
                .buscarPorId(id);
    }
}