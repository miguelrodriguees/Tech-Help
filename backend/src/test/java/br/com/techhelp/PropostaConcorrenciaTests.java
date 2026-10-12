package br.com.techhelp;

import br.com.techhelp.dto.CriarPropostaRequest;
import br.com.techhelp.dto.CriarSolicitacaoRequest;
import br.com.techhelp.service.SolicitacaoService;
import br.com.techhelp.model.Proposta;
import br.com.techhelp.model.Solicitacao;
import br.com.techhelp.service.PropostaService;
import br.com.techhelp.service.ServicoService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.*;

/** Opt-in no MariaDB: cria e remove apenas fixtures próprias, com UUID e IDs registrados. */
@SpringBootTest
@EnabledIfSystemProperty(named = "techhelp.proposal.integration", matches = "true")
class PropostaConcorrenciaTests {
    @Autowired ServicoService servicos;
    @Autowired PropostaService propostas;
    @Autowired SolicitacaoService solicitacoes;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager manager;

    @Test void duasPropostasDoMesmoPedido() throws Exception { disputar(false, false); }
    @Test void doisAceitesDaMesmaProposta() throws Exception { disputar(true, false); }
    @Test void envioSimultaneoAoAceite() throws Exception { disputar(false, true); }

    private long inserir(String sql, Object... values) {
        var key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) statement.setObject(i + 1, values[i]);
            return statement;
        }, key);
        return Objects.requireNonNull(key.getKey()).longValue();
    }

    private void disputar(boolean mesma, boolean envio) throws Exception {
        assertThat(jdbc.queryForObject("select database()", String.class))
                .isEqualTo(System.getProperty("techhelp.integration.database"));
        String tag = "TESTE-PROPOSTA-" + UUID.randomUUID();
        List<Long> users = new ArrayList<>(), techs = new ArrayList<>();
        Long client = null, category = null, request = null;
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            for (int i = 0; i < 3; i++) {
                long user = inserir("insert into usuario(nome,email,senha_hash,cpf) values (?,?,?,?)",
                        tag, tag + i + "@example.invalid", "CONTA-TESTE-SEM-LOGIN",
                        String.format("%011d", ThreadLocalRandom.current().nextLong(100000000000L)));
                users.add(user);
                jdbc.update("insert into usuario_perfil(id_usuario,id_perfil) select ?,id_perfil from perfil where nome=?",
                        user, i == 0 ? "CLIENTE" : "TECNICO");
                if (i == 0) client = inserir("insert into cliente(id_usuario) values (?)", user);
                else techs.add(inserir("insert into tecnico(id_usuario) values (?)", user));
            }
            category = inserir("insert into categoria(nome) values (?)", tag);
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(tag + "0@example.invalid", null, List.of()));
            var criada = solicitacoes.criar(new CriarSolicitacaoRequest(client, category, null, tag, tag, "REMOTO", "NORMAL", null, null));
            request = criada.getIdSolicitacao();
            assertThat(criada.getDataCadastro()).isNotNull();
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(tag + "1@example.invalid", null, List.of()));
            var enviada = propostas.criar(new CriarPropostaRequest(request, techs.get(0), new BigDecimal("100.00"), tag, null, null));
            long first = enviada.getIdProposta();
            assertThat(enviada.getDataCadastro()).isNotNull();
            SecurityContextHolder.clearContext();
            long second = mesma || envio ? first : inserir("insert into proposta(id_solicitacao,id_tecnico,valor) values (?,?,200)", request, techs.get(1));
            long requestId = request, secondTech = techs.get(1);
            CountDownLatch ready = new CountDownLatch(2), go = new CountDownLatch(1);
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                final int worker = i;
                results.add(executor.submit(() -> {
                    String email = tag + (envio && worker == 1 ? 2 : 0) + "@example.invalid";
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(email, null, List.of()));
                    try {
                        var tx = new TransactionTemplate(manager);
                        tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
                        tx.executeWithoutResult(status -> {
                            // As duas transações começam com entidades antigas já no contexto JPA.
                            em.find(Solicitacao.class, requestId);
                            em.find(Proposta.class, worker == 0 ? first : second);
                            ready.countDown();
                            try {
                                if (!go.await(15, TimeUnit.SECONDS)) throw new IllegalStateException("Barreira expirou");
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt(); throw new IllegalStateException(e);
                            }
                            if (envio && worker == 1) {
                                propostas.criar(new CriarPropostaRequest(requestId, secondTech,
                                        new BigDecimal("200.00"), tag, null, null));
                            } else assertThat(servicos.aceitarProposta(worker == 0 ? first : second).getDataCadastro()).isNotNull();
                        });
                        return true;
                    } catch (IllegalArgumentException expected) { return false; }
                    finally { SecurityContextHolder.clearContext(); }
                }));
            }
            assertThat(ready.await(15, TimeUnit.SECONDS)).isTrue();
            go.countDown();
            boolean a = results.get(0).get(30, TimeUnit.SECONDS);
            boolean b = results.get(1).get(30, TimeUnit.SECONDS);
            if (envio) assertThat(a).isTrue();
            else assertThat(a ^ b).as("exatamente um aceite deve vencer").isTrue();
            assertThat(jdbc.queryForObject("select status from solicitacao where id_solicitacao=?", String.class, request)).isEqualTo("CONTRATADA");
            assertThat(jdbc.queryForObject("select count(*) from proposta where id_solicitacao=? and status='ACEITA'", Integer.class, request)).isEqualTo(1);
            assertThat(jdbc.queryForObject("select count(*) from proposta where id_solicitacao=? and status='ENVIADA'", Integer.class, request)).isZero();
            assertThat(jdbc.queryForObject("select count(*) from servico s join proposta p on p.id_proposta=s.id_proposta where p.id_solicitacao=?", Integer.class, request)).isEqualTo(1);
            var chosen = jdbc.queryForMap("select s.id_tecnico,s.valor_acordado,p.id_tecnico as proponente,p.valor from servico s join proposta p on p.id_proposta=s.id_proposta where p.id_solicitacao=?", request);
            assertThat(chosen.get("id_tecnico")).isEqualTo(chosen.get("proponente"));
            assertThat((BigDecimal) chosen.get("valor_acordado")).isEqualByComparingTo((BigDecimal) chosen.get("valor"));
        } finally {
            SecurityContextHolder.clearContext();
            executor.shutdownNow();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) throw new IllegalStateException("Preservar fixtures, threads ativas: " + tag);
            if (request != null) {
                jdbc.update("delete s from servico s join proposta p on p.id_proposta=s.id_proposta where p.id_solicitacao=?", request);
                jdbc.update("delete from proposta where id_solicitacao=?", request);
                jdbc.update("delete from solicitacao where id_solicitacao=?", request);
            }
            if (category != null) jdbc.update("delete from categoria where id_categoria=?", category);
            for (long id : techs) jdbc.update("delete from tecnico where id_tecnico=?", id);
            if (client != null) jdbc.update("delete from cliente where id_cliente=?", client);
            for (long id : users) {
                jdbc.update("delete from usuario_perfil where id_usuario=?", id);
                jdbc.update("delete from usuario where id_usuario=?", id);
            }
        }
    }
}
