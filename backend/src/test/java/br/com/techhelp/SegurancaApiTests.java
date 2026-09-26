package br.com.techhelp;

import br.com.techhelp.config.SecurityConfig;
import br.com.techhelp.config.WebConfig;
import br.com.techhelp.controller.*;
import br.com.techhelp.model.*;
import br.com.techhelp.repository.*;
import br.com.techhelp.service.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import({SecurityConfig.class, WebConfig.class, AcessoService.class, AutenticacaoService.class,
        AluguelItemService.class,
        AluguelService.class,
        AvaliacaoService.class,
        CadastroService.class,
        CategoriaService.class,
        CertificacaoService.class,
        ClienteService.class,
        ConversaService.class,
        DenunciaService.class,
        EnderecoService.class,
        EspecialidadeService.class,
        FavoritoService.class,
        FerramentaService.class,
        KitFerramentaService.class,
        KitService.class,
        MensagemService.class,
        NotificacaoService.class,
        PagamentoService.class,
        PerfilService.class,
        PortfolioService.class,
        PropostaService.class,
        ServicoService.class,
        SolicitacaoAnexoService.class,
        SolicitacaoService.class,
        TecnicoEspecialidadeService.class,
        TecnicoService.class,
        UsuarioService.class})
class SegurancaApiTests {
    private static final String HASH = new BCryptPasswordEncoder().encode("SenhaDeTeste!");
    @Autowired MockMvc mvc;
    @MockitoBean UsuarioRepository usuarios;
    @MockitoBean UsuarioPerfilRepository vinculos;
    @MockitoBean PerfilRepository perfis;
    @MockitoBean ClienteRepository clientes;
    @MockitoBean TecnicoRepository tecnicos;
    @MockitoBean SolicitacaoRepository solicitacoes;
    @MockitoBean PropostaRepository propostas;
    @MockitoBean ServicoRepository servicos;
    @MockitoBean AluguelRepository alugueis;
    @MockitoBean EnderecoRepository enderecos;
    @MockitoBean ParticipanteConversaRepository participantes;
    @MockitoBean NotificacaoRepository notificacoes;
    @MockitoBean PagamentoRepository pagamentos;
    @MockitoBean PortfolioRepository portfolios;
    @MockitoBean CertificacaoRepository certificacoes;
    @MockitoBean DenunciaRepository denuncias;
    @MockitoBean SolicitacaoAnexoRepository anexos;
    @MockitoBean CategoriaRepository categorias;
    @MockitoBean AvaliacaoRepository avaliacoes;
    @MockitoBean AluguelItemRepository aluguelItens;
    @MockitoBean ConversaRepository conversas;
    @MockitoBean FerramentaRepository ferramentas;
    @MockitoBean KitRepository kits;
    @MockitoBean KitFerramentaRepository kitFerramentas;
    @MockitoBean MensagemRepository mensagens;
    @MockitoBean TecnicoEspecialidadeRepository tecnicoEspecialidades;
    @MockitoBean EspecialidadeRepository especialidades;
    @MockitoBean FavoritoRepository favoritos;
    private final Map<Long, Usuario> contas = new HashMap<>();
    private final Map<Long, String> papeis = new HashMap<>();
    private Solicitacao solicitacao;
    private Servico servico;

    @BeforeEach
    void preparar() {
        contas.clear();
        papeis.clear();
        for (long id = 1; id <= 4; id++) {
            Usuario u = new Usuario();
            u.setIdUsuario(id);
            u.setEmail("u" + id + "@example.invalid");
            u.setNome("Usuario " + id);
            u.setSenhaHash(HASH);
            u.setStatus("ATIVO");
            contas.put(id, u);
        }
        papeis.put(1L, "CLIENTE");
        papeis.put(2L, "TECNICO");
        papeis.put(3L, "CLIENTE");
        papeis.put(4L, "ADMIN");
        when(usuarios.findByEmail(anyString())).thenAnswer(inv ->
                contas.values().stream().filter(u -> u.getEmail().equals(inv.getArgument(0))).findFirst());
        when(usuarios.existsById(anyLong())).thenAnswer(inv -> contas.containsKey(inv.getArgument(0)));
        when(vinculos.findByIdUsuario(anyLong())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            if (!papeis.containsKey(id)) return List.of();
            UsuarioPerfil v = new UsuarioPerfil(); v.setIdUsuario(id); v.setIdPerfil(id);
            return List.of(v);
        });
        when(perfis.findAllById(any())).thenAnswer(inv -> {
            List<Perfil> lista = new ArrayList<>();
            Iterable<Long> ids = inv.getArgument(0);
            for (Long id : ids) {
                Perfil p = new Perfil(); ReflectionTestUtils.setField(p, "idPerfil", id);
                p.setNome(papeis.get(id)); lista.add(p);
            }
            return lista;
        });
        Cliente cliente = new Cliente(); cliente.setIdCliente(10L); cliente.setIdUsuario(1L);
        Cliente outro = new Cliente(); outro.setIdCliente(30L); outro.setIdUsuario(3L);
        when(clientes.findById(10L)).thenReturn(Optional.of(cliente));
        when(clientes.findById(30L)).thenReturn(Optional.of(outro));
        when(clientes.findByIdUsuario(1L)).thenReturn(Optional.of(cliente));
        when(clientes.existsById(10L)).thenReturn(true);
        Tecnico tecnico = new Tecnico(); tecnico.setIdTecnico(20L); tecnico.setIdUsuario(2L);
        when(tecnicos.findById(20L)).thenReturn(Optional.of(tecnico));
        when(tecnicos.findByIdUsuario(2L)).thenReturn(Optional.of(tecnico));
        when(tecnicos.existsByIdUsuario(2L)).thenReturn(true);
        when(tecnicos.existsById(20L)).thenReturn(true);
        Categoria categoria = new Categoria(); categoria.setIdCategoria(1L); categoria.setNome("Hardware");
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));
        when(categorias.findByAtivoTrueOrderByNomeAsc()).thenReturn(List.of(categoria));
        solicitacao = new Solicitacao(); solicitacao.setIdSolicitacao(100L);
        solicitacao.setIdCliente(10L); solicitacao.setStatus("ABERTA");
        when(solicitacoes.findById(100L)).thenReturn(Optional.of(solicitacao));
        when(solicitacoes.existsById(100L)).thenReturn(true);
        when(solicitacoes.save(any())).thenAnswer(inv -> {
            Solicitacao s = inv.getArgument(0); if (s.getIdSolicitacao() == null) s.setIdSolicitacao(101L); return s;
        });
        Proposta proposta = new Proposta(); proposta.setIdProposta(200L);
        proposta.setIdSolicitacao(100L); proposta.setIdTecnico(20L);
        proposta.setStatus("ENVIADA"); proposta.setValor(new BigDecimal("100"));
        when(propostas.findById(200L)).thenReturn(Optional.of(proposta));
        when(propostas.save(any())).thenAnswer(inv -> inv.getArgument(0));
        servico = new Servico(); ReflectionTestUtils.setField(servico, "idServico", 300L); servico.setIdProposta(200L);
        servico.setIdCliente(10L); servico.setIdTecnico(20L); servico.setStatus("AGENDADO");
        when(servicos.findById(300L)).thenReturn(Optional.of(servico));
        when(servicos.save(any())).thenAnswer(inv -> {
            Servico s = inv.getArgument(0); ReflectionTestUtils.setField(s, "idServico", 300L); servico = s; return s;
        });
        when(avaliacoes.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private MockHttpSession login(long id) throws Exception {
        var res = mvc.perform(post("/auth/login").with(csrf())
                .param("email", "u" + id + "@example.invalid").param("senha", "SenhaDeTeste!"))
                .andExpect(status().isNoContent()).andReturn();
        return (MockHttpSession) res.getRequest().getSession(false);
    }

    @Test
    void catalogoPublicoContinuaDisponivel() throws Exception {
        mvc.perform(get("/categorias")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Hardware"));
    }

    @Test
    void rotasPrivadasExigemLogin() throws Exception {
        for (String path : List.of("/auth/me", "/solicitacoes/100", "/solicitacoes/abertas",
                "/servicos/300", "/perfis", "/usuarios/1", "/conversas/1")) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/solicitacoes").with(csrf()).contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
        verify(solicitacoes, never()).save(any());
    }

    @Test
    void loginRealPersisteSessaoSemExporSenhaOuCpf() throws Exception {
        var session = login(1);
        assertThat(session).isNotNull();
        mvc.perform(get("/auth/me").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.idCliente").value(10))
                .andExpect(jsonPath("$.perfis[0]").value("CLIENTE"))
                .andExpect(jsonPath("$.senhaHash").doesNotExist())
                .andExpect(jsonPath("$.cpf").doesNotExist());
    }

    @Test
    void loginNormalizaEmail() throws Exception {
        mvc.perform(post("/auth/login").with(csrf())
                .param("email", " U1@EXAMPLE.INVALID ").param("senha", "SenhaDeTeste!"))
                .andExpect(status().isNoContent());
    }

    @Test
    void senhaErradaEmailInexistenteEContaBloqueadaRetornamErroGenerico() throws Exception {
        mvc.perform(post("/auth/login").with(csrf()).param("email", "u1@example.invalid")
                .param("senha", "incorreta")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("E-mail ou senha invalidos"));
        mvc.perform(post("/auth/login").with(csrf()).param("email", "inexistente@example.invalid")
                .param("senha", "SenhaDeTeste!")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("E-mail ou senha invalidos"));
        contas.get(1L).setStatus("BLOQUEADO");
        mvc.perform(post("/auth/login").with(csrf()).param("email", "u1@example.invalid")
                .param("senha", "SenhaDeTeste!")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("E-mail ou senha invalidos"));
    }

    @Test
    void csrfEObrigatorioInclusiveNoLogin() throws Exception {
        mvc.perform(post("/auth/login").param("email", "u1@example.invalid")
                .param("senha", "SenhaDeTeste!")).andExpect(status().isForbidden());
        mvc.perform(post("/solicitacoes").session(login(1)).contentType("application/json")
                .content("{}")).andExpect(status().isForbidden());
    }

    @Test
    void csrfRealERotacaoDaSessaoNoLogin() throws Exception {
        var res = mvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn();
        var token = (org.springframework.security.web.csrf.CsrfToken)
                res.getRequest().getAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName());
        var session = (MockHttpSession) res.getRequest().getSession(false);
        String antes = session.getId();
        mvc.perform(post("/auth/login").session(session)
                .header(token.getHeaderName(), token.getToken())
                .param("email", "u1@example.invalid").param("senha", "SenhaDeTeste!"))
                .andExpect(status().isNoContent());
        assertThat(session.getId()).isNotEqualTo(antes);
        mvc.perform(post("/auth/logout").session(session)
                .header(token.getHeaderName(), token.getToken())).andExpect(status().isForbidden());
        var novo = mvc.perform(get("/auth/csrf").session(session)).andReturn();
        var renovado = (org.springframework.security.web.csrf.CsrfToken)
                novo.getRequest().getAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName());
        mvc.perform(post("/auth/logout").session(session)
                .header(renovado.getHeaderName(), renovado.getToken())).andExpect(status().isNoContent());
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void clientePodeCriarEmSeuNomeMasNaoEmNomeDeOutro() throws Exception {
        var session = login(1);
        String body = """
                {"idCliente":10,"idCategoria":1,"titulo":"Teste","descricao":"Problema",
                 "tipoAtendimento":"REMOTO"}
                """;
        mvc.perform(post("/solicitacoes").session(session).with(csrf())
                .contentType("application/json").content(body)).andExpect(status().isCreated());
        mvc.perform(post("/solicitacoes").session(session).with(csrf())
                .contentType("application/json").content(body.replace(":10", ":30")))
                .andExpect(status().isForbidden());
        verify(solicitacoes, times(1)).save(any());
    }

    @Test
    void outroClienteNaoLeSolicitacaoNemListaPropostasOuAceita() throws Exception {
        var session = login(3);
        mvc.perform(get("/solicitacoes/100").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/propostas/solicitacao/100").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/servicos/aceitar-proposta/200").session(session).with(csrf()))
                .andExpect(status().isForbidden());
        verify(servicos, never()).save(any());
    }

    @Test
    void tecnicoLeOportunidadeMasNaoAceitaPropostaPeloCliente() throws Exception {
        var session = login(2);
        mvc.perform(get("/solicitacoes/100").session(session)).andExpect(status().isOk());
        mvc.perform(post("/servicos/aceitar-proposta/200").session(session).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/propostas/solicitacao/100").session(session)).andExpect(status().isForbidden());
    }

    @Test
    void tecnicoNaoEnviaPropostaComIdentidadeAlheia() throws Exception {
        mvc.perform(post("/propostas").session(login(2)).with(csrf()).contentType("application/json")
                .content("{\"idSolicitacao\":100,\"idTecnico\":999,\"valor\":100}"))
                .andExpect(status().isForbidden());
        verify(propostas, never()).save(any());
    }

    @Test
    void clienteNaoExecutaServicoETecnicoResponsavelExecuta() throws Exception {
        mvc.perform(post("/servicos/300/iniciar").session(login(1)).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/servicos/300/iniciar").session(login(2)).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
        mvc.perform(post("/servicos/300/concluir").session(login(2)).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONCLUIDO"));
        assertThat(solicitacao.getStatus()).isEqualTo("CONCLUIDA");
    }

    @Test
    void somenteParticipanteAvaliaEmSeuNomeAposConclusao() throws Exception {
        var session = login(1);
        String body = "{\"idServico\":300,\"idAvaliador\":1,\"idAvaliado\":2,\"nota\":5}";
        mvc.perform(post("/avaliacoes").session(session).with(csrf())
                .contentType("application/json").content(body)).andExpect(status().isBadRequest());
        servico.setStatus("CONCLUIDO");
        mvc.perform(post("/avaliacoes").session(session).with(csrf())
                .contentType("application/json").content(body)).andExpect(status().isCreated());
        mvc.perform(post("/avaliacoes").session(login(3)).with(csrf())
                .contentType("application/json").content(body)).andExpect(status().isForbidden());
        verify(avaliacoes, times(1)).save(any());
    }

    @Test
    void vinculoDePerfilExigeAdminERevogacaoValeNaSessaoExistente() throws Exception {
        String body = "{\"idUsuario\":1,\"idPerfil\":4}";
        mvc.perform(post("/perfis/vinculos").session(login(1)).with(csrf())
                .contentType("application/json").content(body)).andExpect(status().isForbidden());
        verify(vinculos, never()).save(any());
        when(perfis.existsById(4L)).thenReturn(true);
        when(vinculos.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var admin = login(4);
        mvc.perform(post("/perfis/vinculos").session(admin).with(csrf())
                .contentType("application/json").content(body)).andExpect(status().isCreated());
        papeis.put(4L, "CLIENTE");
        mvc.perform(post("/perfis/vinculos").session(admin).with(csrf())
                .contentType("application/json").content(body)).andExpect(status().isForbidden());
        verify(vinculos, times(1)).save(any());
    }

    @Test
    void bloqueioDaContaValeParaSessaoExistente() throws Exception {
        var session = login(1);
        contas.get(1L).setStatus("BLOQUEADO");
        mvc.perform(get("/auth/me").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/solicitacoes/100").session(session)).andExpect(status().isForbidden());
    }

    @Test
    void corsPermiteCredenciaisApenasParaOrigemConfigurada() throws Exception {
        mvc.perform(options("/auth/login").header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        mvc.perform(options("/auth/login").header("Origin", "https://outro.example")
                .header("Access-Control-Request-Method", "POST")).andExpect(status().isForbidden());
    }

    @Test
    void consultaDoProprioUsuarioNaoExpoeHashERejeitaOutroUsuario() throws Exception {
        when(usuarios.findById(1L)).thenReturn(Optional.of(contas.get(1L)));
        mvc.perform(get("/usuarios/1").session(login(1))).andExpect(status().isOk())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
        mvc.perform(get("/usuarios/1").session(login(3))).andExpect(status().isForbidden());
    }

    @Test
    void cadastroPublicoGravaBcryptEPerfilClienteSemPermitirEscolherAdmin() throws Exception {
        Perfil perfil = new Perfil(); ReflectionTestUtils.setField(perfil, "idPerfil", 1L);
        perfil.setNome("CLIENTE");
        when(perfis.findByNome("CLIENTE")).thenReturn(Optional.of(perfil));
        when(usuarios.save(any())).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0); u.setIdUsuario(5L); return u;
        });
        when(clientes.save(any())).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0); c.setIdCliente(50L); return c;
        });
        mvc.perform(post("/cadastro/cliente").with(csrf()).contentType("application/json").content("""
                {"nome":"Novo cliente","email":"novo@example.invalid","senha":"SenhaDeTeste!",
                 "cpf":"12345678901","perfil":"ADMIN"}
                """)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
        verify(usuarios).save(argThat(u -> new BCryptPasswordEncoder()
                .matches("SenhaDeTeste!", u.getSenhaHash())));
        verify(vinculos).save(argThat(v -> v.getIdUsuario().equals(5L) && v.getIdPerfil().equals(1L)));
    }

    @Test
    void fluxoAutenticadoDaSolicitacaoAteAvaliacao() throws Exception {
        var cliente = login(1);
        var tecnico = login(2);
        doAnswer(inv -> {
            solicitacao = inv.getArgument(0); solicitacao.setIdSolicitacao(100L);
            return solicitacao;
        }).when(solicitacoes).save(any());
        when(solicitacoes.findById(100L)).thenAnswer(inv -> Optional.of(solicitacao));
        var propostaCriada = new java.util.concurrent.atomic.AtomicReference<Proposta>();
        doAnswer(inv -> {
            Proposta p = inv.getArgument(0); p.setIdProposta(200L);
            propostaCriada.set(p); return p;
        }).when(propostas).save(any());
        when(propostas.findById(200L)).thenAnswer(inv -> Optional.ofNullable(propostaCriada.get()));
        when(servicos.findById(300L)).thenAnswer(inv -> Optional.of(servico));
        mvc.perform(post("/solicitacoes").session(cliente).with(csrf()).contentType("application/json")
                .content("""
                        {"idCliente":10,"idCategoria":1,"titulo":"Computador lento",
                         "descricao":"Preciso de suporte","tipoAtendimento":"REMOTO"}
                        """)).andExpect(status().isCreated());
        mvc.perform(post("/propostas").session(tecnico).with(csrf()).contentType("application/json")
                .content("{\"idSolicitacao\":100,\"idTecnico\":20,\"valor\":100}"))
                .andExpect(status().isCreated());
        assertThat(solicitacao.getStatus()).isEqualTo("EM_NEGOCIACAO");
        mvc.perform(post("/servicos/aceitar-proposta/200").session(cliente).with(csrf()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("AGENDADO"));
        mvc.perform(post("/servicos/300/concluir").session(tecnico).with(csrf()))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/servicos/300/iniciar").session(tecnico).with(csrf()))
                .andExpect(status().isOk());
        mvc.perform(post("/servicos/300/concluir").session(tecnico).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONCLUIDO"));
        mvc.perform(post("/avaliacoes").session(cliente).with(csrf()).contentType("application/json")
                .content("""
                        {"idServico":300,"idAvaliador":1,"idAvaliado":2,"nota":5,"comentario":"Resolvido"}
                        """)).andExpect(status().isCreated());
        verify(avaliacoes).save(any());
    }

    @Test
    void recursosPessoaisSoPodemSerLidosPeloDono() throws Exception {
        Endereco endereco = new Endereco(); endereco.setIdUsuario(1L);
        Aluguel aluguel = new Aluguel(); aluguel.setIdUsuario(1L);
        when(enderecos.findById(500L)).thenReturn(Optional.of(endereco));
        when(alugueis.findById(600L)).thenReturn(Optional.of(aluguel));
        var dono = login(1);
        var outro = login(3);
        for (String path : List.of("/enderecos/500", "/alugueis/600")) {
            mvc.perform(get(path).session(dono)).andExpect(status().isOk());
            mvc.perform(get(path).session(outro)).andExpect(status().isForbidden());
        }
        mvc.perform(get("/conversas/700").session(outro)).andExpect(status().isForbidden());
        mvc.perform(get("/conversas/700/mensagens").param("idUsuario", "1").session(outro))
                .andExpect(status().isForbidden());
        verify(mensagens, never()).findByIdConversaOrderByDataEnvioAsc(anyLong());
    }

    @Test
    void participanteEnviaMensagemMasNaoPodeImitarOutroUsuarioOuSistema() throws Exception {
        Conversa conversa = new Conversa(); conversa.setStatus("ATIVA");
        when(conversas.findById(700L)).thenReturn(Optional.of(conversa));
        when(participantes.existsByIdConversaAndIdUsuario(700L, 1L)).thenReturn(true);
        when(mensagens.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var cliente = login(1);
        String texto = "{\"idUsuario\":1,\"texto\":\"Ola\",\"tipo\":\"TEXTO\"}";
        mvc.perform(post("/conversas/700/mensagens").session(cliente).with(csrf())
                .contentType("application/json").content(texto)).andExpect(status().isCreated());
        mvc.perform(post("/conversas/700/mensagens").session(cliente).with(csrf())
                .contentType("application/json").content(texto.replace(":1", ":2")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/conversas/700/mensagens").session(cliente).with(csrf())
                .contentType("application/json").content(texto.replace("TEXTO", "SISTEMA")))
                .andExpect(status().isForbidden());
        verify(mensagens, times(1)).save(any());
    }

    @Test
    void clienteNaoConfirmaRetiradaDevolucaoOuPagamentoSimulado() throws Exception {
        var cliente = login(1);
        for (String path : List.of("/alugueis/600/retirar", "/alugueis/600/devolver",
                "/pagamentos/800/pagar", "/pagamentos/800/cancelar")) {
            mvc.perform(post(path).session(cliente).with(csrf())).andExpect(status().isForbidden());
        }
        verify(alugueis, never()).save(any());
        verify(pagamentos, never()).save(any());
    }
}
