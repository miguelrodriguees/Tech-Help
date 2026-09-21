package br.com.techhelp.controller;

import br.com.techhelp.repository.*;
import br.com.techhelp.service.ClienteAutenticado;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cliente/solicitacoes")
public class PropostasClienteController {
    private final ClienteAutenticado cliente;
    private final SolicitacaoRepository pedidos;
    private final PropostaRepository propostas;
    private final TecnicoRepository tecnicos;
    private final UsuarioRepository usuarios;
    public PropostasClienteController(ClienteAutenticado cliente, SolicitacaoRepository pedidos,
            PropostaRepository propostas, TecnicoRepository tecnicos, UsuarioRepository usuarios) {
        this.cliente=cliente; this.pedidos=pedidos; this.propostas=propostas;
        this.tecnicos=tecnicos; this.usuarios=usuarios;
    }
    public record Oferta(Long idProposta, String nomeProfissional, BigDecimal valor, String mensagem,
                        Short prazoEstimadoDias, LocalDate dataDisponivel, String status) {}
    @GetMapping("/{id}/propostas")
    public List<Oferta> listar(@PathVariable Long id, Principal principal) {
        Long idCliente=cliente.id(principal);
        pedidos.findById(id).filter(p -> p.getIdCliente().equals(idCliente))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return propostas.findByIdSolicitacaoOrderByDataCadastroDesc(id).stream().map(p -> {
            String nome=tecnicos.findById(p.getIdTecnico()).flatMap(t -> usuarios.findById(t.getIdUsuario()))
                    .map(u -> u.getNome()).orElse("Profissional indisponível");
            return new Oferta(p.getIdProposta(),nome,p.getValor(),p.getMensagem(),p.getPrazoEstimadoDias(),p.getDataDisponivel(),p.getStatus());
        }).toList();
    }
}
