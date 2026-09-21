package br.com.techhelp.controller;

import br.com.techhelp.repository.SolicitacaoRepository;
import br.com.techhelp.repository.TecnicoRepository;
import br.com.techhelp.repository.ClienteRepository;
import br.com.techhelp.service.TecnicoAutenticado;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/tecnico/solicitacoes")
public class OportunidadesController {
    private final TecnicoAutenticado autenticado;
    private final SolicitacaoRepository solicitacoes;
    private final TecnicoRepository tecnicos;
    private final ClienteRepository clientes;
    public OportunidadesController(TecnicoAutenticado autenticado, SolicitacaoRepository solicitacoes,
                                  TecnicoRepository tecnicos, ClienteRepository clientes) {
        this.autenticado = autenticado; this.solicitacoes = solicitacoes;
        this.tecnicos = tecnicos; this.clientes = clientes;
    }
    public record Oportunidade(Long idSolicitacao, Long idCategoria, String titulo,
                               String descricao, String tipoAtendimento, String urgencia, String status) {}
    @GetMapping
    public List<Oportunidade> listar(Principal principal) {
        Long id = autenticado.id(principal);
        var tecnico = tecnicos.findById(id).orElseThrow();
        Long proprioCliente = clientes.findByIdUsuario(tecnico.getIdUsuario()).map(c -> c.getIdCliente()).orElse(null);
        return solicitacoes.findByStatusInOrderByDataCadastroDesc(List.of("ABERTA", "EM_NEGOCIACAO"))
            .stream().filter(s -> !s.getIdCliente().equals(proprioCliente))
            .map(s -> new Oportunidade(s.getIdSolicitacao(), s.getIdCategoria(), s.getTitulo(),
                    s.getDescricao(), s.getTipoAtendimento(), s.getUrgencia(), s.getStatus())).toList();
    }
}
