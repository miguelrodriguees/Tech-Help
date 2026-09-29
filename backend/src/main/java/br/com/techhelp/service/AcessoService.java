package br.com.techhelp.service;

import br.com.techhelp.dto.SessaoResponse;
import br.com.techhelp.model.Usuario;
import br.com.techhelp.repository.*;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Confere identidade e propriedade no banco, sem confiar nos IDs enviados pelo cliente.
 * Usado pelas anotacoes PreAuthorize dos servicos.
 */
@Service("acesso")
public class AcessoService {
    private final UsuarioRepository usuarios;
    private final UsuarioPerfilRepository vinculos;
    private final PerfilRepository perfis;
    private final ClienteRepository clientes;
    private final TecnicoRepository tecnicos;
    private final SolicitacaoRepository solicitacoes;
    private final PropostaRepository propostas;
    private final ServicoRepository servicos;
    private final AluguelRepository alugueis;
    private final EnderecoRepository enderecos;
    private final ParticipanteConversaRepository participantes;
    private final NotificacaoRepository notificacoes;
    private final PagamentoRepository pagamentos;
    private final PortfolioRepository portfolios;
    private final CertificacaoRepository certificacoes;
    private final DenunciaRepository denuncias;
    private final SolicitacaoAnexoRepository anexos;

    public AcessoService(
            UsuarioRepository usuarios,
            UsuarioPerfilRepository vinculos,
            PerfilRepository perfis,
            ClienteRepository clientes,
            TecnicoRepository tecnicos,
            SolicitacaoRepository solicitacoes,
            PropostaRepository propostas,
            ServicoRepository servicos,
            AluguelRepository alugueis,
            EnderecoRepository enderecos,
            ParticipanteConversaRepository participantes,
            NotificacaoRepository notificacoes,
            PagamentoRepository pagamentos,
            PortfolioRepository portfolios,
            CertificacaoRepository certificacoes,
            DenunciaRepository denuncias,
            SolicitacaoAnexoRepository anexos) {
        this.usuarios = usuarios;
        this.vinculos = vinculos;
        this.perfis = perfis;
        this.clientes = clientes;
        this.tecnicos = tecnicos;
        this.solicitacoes = solicitacoes;
        this.propostas = propostas;
        this.servicos = servicos;
        this.alugueis = alugueis;
        this.enderecos = enderecos;
        this.participantes = participantes;
        this.notificacoes = notificacoes;
        this.pagamentos = pagamentos;
        this.portfolios = portfolios;
        this.certificacoes = certificacoes;
        this.denuncias = denuncias;
        this.anexos = anexos;
    }

    private Usuario atual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Autenticacao necessaria");
        }
        return usuarios.findByEmail(auth.getName())
                .filter(u -> "ATIVO".equals(u.getStatus()))
                .orElseThrow(() -> new AccessDeniedException("Conta indisponivel"));
    }

    private List<String> perfis(Usuario usuario) {
        var ids = vinculos.findByIdUsuario(usuario.getIdUsuario()).stream()
                .map(v -> v.getIdPerfil()).toList();
        return perfis.findAllById(ids).stream().map(p -> p.getNome()).toList();
    }

    public boolean admin() {
        return perfis(atual()).contains("ADMIN");
    }

    public boolean mesmoUsuario(Long id) {
        return atual().getIdUsuario().equals(id);
    }

    public boolean usuario(Long id) {
        return mesmoUsuario(id) || admin();
    }

    public boolean cliente(Long id) {
        var u = atual();
        return admin() || (perfis(u).contains("CLIENTE") && clientes.findById(id)
                .map(c -> c.getIdUsuario().equals(u.getIdUsuario())).orElse(false));
    }

    public boolean tecnico(Long id) {
        var u = atual();
        return admin() || (perfis(u).contains("TECNICO") && tecnicos.findById(id)
                .map(t -> t.getIdUsuario().equals(u.getIdUsuario())).orElse(false));
    }

    public boolean profissional() {
        var u = atual();
        return admin() || (perfis(u).contains("TECNICO")
                && tecnicos.existsByIdUsuario(u.getIdUsuario()));
    }

    public boolean donoSolicitacao(Long id) {
        return admin() || solicitacoes.findById(id).map(s -> cliente(s.getIdCliente())).orElse(false);
    }

    public boolean lerSolicitacao(Long id) {
        if (admin()) return true;
        return solicitacoes.findById(id).map(s -> {
            if (cliente(s.getIdCliente())) return true;
            if (!profissional()) return false;
            if (List.of("ABERTA", "EM_NEGOCIACAO").contains(s.getStatus())) return true;
            return tecnicos.findByIdUsuario(atual().getIdUsuario())
                    .map(t -> propostas.existsByIdSolicitacaoAndIdTecnico(id, t.getIdTecnico()))
                    .orElse(false);
        }).orElse(false);
    }

    public boolean aceitarProposta(Long id) {
        return admin() || propostas.findById(id)
                .map(p -> donoSolicitacao(p.getIdSolicitacao())).orElse(false);
    }

    public boolean servico(Long id) {
        return admin() || servicos.findById(id)
                .map(s -> cliente(s.getIdCliente()) || tecnico(s.getIdTecnico())).orElse(false);
    }

    public boolean executarServico(Long id) {
        return admin() || servicos.findById(id).map(s -> tecnico(s.getIdTecnico())).orElse(false);
    }

    public boolean aluguel(Long id) {
        return admin() || alugueis.findById(id).map(a -> mesmoUsuario(a.getIdUsuario())).orElse(false);
    }

    public boolean endereco(Long id) {
        return admin() || enderecos.findById(id).map(e -> mesmoUsuario(e.getIdUsuario())).orElse(false);
    }

    public boolean conversa(Long id) {
        return admin() || participantes.existsByIdConversaAndIdUsuario(id, atual().getIdUsuario());
    }

    public boolean criarConversa(Long idSolicitacao, Long usuario1, Long usuario2) {
        // Uma conversa da solicitacao deve ligar seu cliente a um tecnico proponente.
        var eu = atual().getIdUsuario();
        if (!eu.equals(usuario1) && !eu.equals(usuario2)) return false;
        return solicitacoes.findById(idSolicitacao).flatMap(s -> clientes.findById(s.getIdCliente()))
                .map(c -> {
                    Long outro;
                    if (c.getIdUsuario().equals(usuario1)) outro = usuario2;
                    else if (c.getIdUsuario().equals(usuario2)) outro = usuario1;
                    else return false;
                    return tecnicos.findByIdUsuario(outro)
                            .map(t -> propostas.existsByIdSolicitacaoAndIdTecnico(idSolicitacao, t.getIdTecnico()))
                            .orElse(false);
                }).orElse(false);
    }

    public boolean notificacao(Long id) {
        return admin() || notificacoes.findById(id).map(n -> mesmoUsuario(n.getIdUsuario())).orElse(false);
    }

    public boolean pagamento(Long id) {
        return admin() || pagamentos.findById(id)
                .map(p -> p.getIdServico() != null ? servico(p.getIdServico()) : aluguel(p.getIdAluguel()))
                .orElse(false);
    }

    public boolean portfolio(Long id) {
        return admin() || portfolios.findById(id).map(p -> tecnico(p.getIdTecnico())).orElse(false);
    }

    public boolean certificacao(Long id) {
        return admin() || certificacoes.findById(id).map(c -> tecnico(c.getIdTecnico())).orElse(false);
    }

    public boolean denuncia(Long id) {
        return admin() || denuncias.findById(id).map(d -> mesmoUsuario(d.getIdDenunciante())).orElse(false);
    }

    public boolean lerAnexo(Long id) {
        return admin() || anexos.findById(id).map(a -> lerSolicitacao(a.getIdSolicitacao())).orElse(false);
    }

    public SessaoResponse sessao() {
        var u = atual();
        return new SessaoResponse(u.getIdUsuario(),
                clientes.findByIdUsuario(u.getIdUsuario()).map(c -> c.getIdCliente()).orElse(null),
                tecnicos.findByIdUsuario(u.getIdUsuario()).map(t -> t.getIdTecnico()).orElse(null),
                u.getNome(), u.getEmail(), perfis(u));
    }
}
