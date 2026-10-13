package br.com.techhelp;

import br.com.techhelp.dto.*;
import br.com.techhelp.service.AvaliacaoService;
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
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@EnabledIfSystemProperty(named = "techhelp.review.integration", matches = "true")
class AvaliacaoConcorrenciaTests {
    @Autowired AvaliacaoService avaliacoes;
    @Autowired JdbcTemplate jdbc;

    private long inserir(String sql, Object... values) {
        var key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) statement.setObject(i + 1, values[i]);
            return statement;
        }, key);
        return Objects.requireNonNull(key.getKey()).longValue();
    }

    @Test void entradasNovaEAntigaNaoDuplicamAvaliacao() throws Exception {
        assertThat(jdbc.queryForObject("select database()", String.class))
                .isEqualTo(System.getProperty("techhelp.integration.database"));
        String tag = "TESTE-AVALIACAO-" + UUID.randomUUID();
        List<Long> users = new ArrayList<>();
        Long client = null, tech = null, category = null, request = null, proposal = null, service = null;
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            for (int i = 0; i < 2; i++) {
                long user = inserir("insert into usuario(nome,email,senha_hash,cpf) values (?,?,?,?)",
                        tag, tag + i + "@example.invalid", "CONTA-TESTE-SEM-LOGIN",
                        String.format("%011d", ThreadLocalRandom.current().nextLong(100000000000L)));
                users.add(user);
                jdbc.update("insert into usuario_perfil(id_usuario,id_perfil) select ?,id_perfil from perfil where nome=?",
                        user, i == 0 ? "CLIENTE" : "TECNICO");
            }
            client = inserir("insert into cliente(id_usuario) values (?)", users.get(0));
            tech = inserir("insert into tecnico(id_usuario) values (?)", users.get(1));
            category = inserir("insert into categoria(nome) values (?)", tag);
            request = inserir("insert into solicitacao(id_cliente,id_categoria,titulo,descricao,tipo_atendimento,status) values (?,?,?,?,'REMOTO','CONCLUIDA')", client, category, tag, tag);
            proposal = inserir("insert into proposta(id_solicitacao,id_tecnico,valor,status) values (?,?,100,'ACEITA')", request, tech);
            service = inserir("insert into servico(id_proposta,id_cliente,id_tecnico,valor_acordado,status) values (?,?,?,100,'CONCLUIDO')", proposal, client, tech);
            long serviceId = service;
            CountDownLatch ready = new CountDownLatch(2), go = new CountDownLatch(1);
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                final int worker = i;
                results.add(executor.submit(() -> {
                    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(tag + "0@example.invalid", null, List.of()));
                    ready.countDown();
                    try {
                        if (!go.await(15, TimeUnit.SECONDS)) throw new IllegalStateException("Barreira expirou");
                        var salva = worker == 0
                                ? avaliacoes.avaliarAtendimento(serviceId, new AvaliarAtendimentoRequest(new BigDecimal("5"), tag))
                                : avaliacoes.criar(new CriarAvaliacaoRequest(serviceId, users.get(0), users.get(1), 4, tag));
                        assertThat(salva.getDataAvaliacao()).isNotNull();
                        return true;
                    } catch (IllegalArgumentException expected) { return false; }
                    finally { SecurityContextHolder.clearContext(); }
                }));
            }
            assertThat(ready.await(15, TimeUnit.SECONDS)).isTrue(); go.countDown();
            boolean first = results.get(0).get(30, TimeUnit.SECONDS), second = results.get(1).get(30, TimeUnit.SECONDS);
            assertThat(first ^ second).isTrue();
            assertThat(jdbc.queryForObject("select count(*) from avaliacao where id_servico=?", Integer.class, service)).isEqualTo(1);
            assertThat(jdbc.queryForObject("select nota from avaliacao where id_servico=?", Integer.class, service)).isEqualTo(first ? 5 : 4);
        } finally {
            executor.shutdownNow();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) throw new IllegalStateException("Threads ativas; preservar fixtures: " + tag);
            if (service != null) {
                jdbc.update("delete from avaliacao where id_servico=?", service);
                jdbc.update("delete from servico where id_servico=?", service);
            }
            if (proposal != null) jdbc.update("delete from proposta where id_proposta=?", proposal);
            if (request != null) jdbc.update("delete from solicitacao where id_solicitacao=?", request);
            if (category != null) jdbc.update("delete from categoria where id_categoria=?", category);
            if (tech != null) jdbc.update("delete from tecnico where id_tecnico=?", tech);
            if (client != null) jdbc.update("delete from cliente where id_cliente=?", client);
            for (long id : users) {
                jdbc.update("delete from usuario_perfil where id_usuario=?", id);
                jdbc.update("delete from usuario where id_usuario=?", id);
            }
        }
    }
}
