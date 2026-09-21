package br.com.techhelp.controller;

import br.com.techhelp.model.*;
import br.com.techhelp.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/atendimentos")
public class AtendimentoController {
    private final AtendimentoService atendimento;
    private final ServicoService servicos;
    private final ClienteAutenticado cliente;
    private final TecnicoAutenticado tecnico;
    public AtendimentoController(AtendimentoService atendimento, ServicoService servicos,
                                 ClienteAutenticado cliente, TecnicoAutenticado tecnico) {
        this.atendimento=atendimento;this.servicos=servicos;this.cliente=cliente;this.tecnico=tecnico;
    }
    @GetMapping("/tecnico")
    public List<Servico> meus(Principal p) {return servicos.listarPorTecnico(tecnico.id(p));}
    @PostMapping("/{id}/iniciar")
    public Servico iniciar(@PathVariable Long id, Principal p) {return atendimento.iniciar(id,tecnico.id(p));}
    @PostMapping("/{id}/concluir")
    public Servico concluir(@PathVariable Long id, Principal p) {return atendimento.concluir(id,cliente.id(p));}
    public record Avaliar(@NotNull @Min(1) @Max(5) Integer nota, @Size(max=3000) String comentario) {}
    @PostMapping("/{id}/avaliacao")
    public Avaliacao avaliar(@PathVariable Long id, @Valid @RequestBody Avaliar dados, Principal p) {
        return atendimento.avaliar(id,cliente.id(p),dados.nota(),dados.comentario());
    }
    @GetMapping("/{id}/avaliacoes/cliente")
    public List<Avaliacao> clienteAvaliacoes(@PathVariable Long id, Principal p) {
        return atendimento.consultarAvaliacoes(id,cliente.id(p),false);
    }
    @GetMapping("/{id}/avaliacoes/tecnico")
    public List<Avaliacao> tecnicoAvaliacoes(@PathVariable Long id, Principal p) {
        return atendimento.consultarAvaliacoes(id,tecnico.id(p),true);
    }
}
