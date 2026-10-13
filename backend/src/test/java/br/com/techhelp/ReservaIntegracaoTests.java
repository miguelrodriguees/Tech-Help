package br.com.techhelp;

import br.com.techhelp.dto.ReservarKitRequest;
import br.com.techhelp.dto.AtualizarFerramentaRequest;
import br.com.techhelp.dto.AtualizarKitRequest;
import br.com.techhelp.model.*;
import br.com.techhelp.repository.*;
import br.com.techhelp.service.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.assertj.core.api.Assertions.*;

/** Opt-in: somente registros próprios, identificados por UUID, removidos no finally. */
@SpringBootTest
@EnabledIfSystemProperty(named = "techhelp.rental.integration", matches = "true")
class ReservaIntegracaoTests {
    @Autowired ReservaService reservas;
    @Autowired AluguelService alugueis;
    @Autowired FerramentaService manutencao;
    @Autowired KitService catalogo;
    @Autowired UsuarioRepository usuarios;
    @Autowired FerramentaRepository ferramentas;
    @Autowired KitRepository kits;
    @Autowired KitFerramentaRepository composicoes;
    @Autowired AluguelRepository aluguelRepository;
    @Autowired JdbcTemplate jdbc;

    private void autenticar(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }

    @Test
    void falhaDesfazAluguelEReservasConcorrentesNaoExcedemFerramentaCompartilhada() throws Exception {
        disputar(0);
    }

    @Test void manutencaoConcorrenteComReserva() throws Exception { disputar(1); }
    @Test void inativacaoDoKitConcorrenteComReserva() throws Exception { disputar(2); }

    // 0: dois kits; 1: manutenção da ferramenta; 2: inativação do kit.
    private void disputar(int cenario) throws Exception {
        assertThat(jdbc.queryForObject("select database()", String.class))
                .isEqualTo(System.getProperty("techhelp.integration.database"));
        String tag = "TESTE-AUTO-" + UUID.randomUUID();
        Long userId = null, toolId = null;
        List<Long> kitIds = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Usuario usuario = new Usuario();
            usuario.setNome(tag); usuario.setEmail(tag + "@example.invalid");
            usuario.setSenhaHash("CONTA-DE-TESTE-SEM-LOGIN");
            usuario.setCpf(String.format("%011d", ThreadLocalRandom.current().nextLong(100000000000L)));
            usuario.setStatus("ATIVO");
            userId = usuarios.saveAndFlush(usuario).getIdUsuario();
            // Perfil apenas da conta UUID temporária, sem credencial de login utilizável.
            jdbc.update("insert into usuario_perfil(id_usuario,id_perfil) select ?,id_perfil from perfil where nome='ADMIN'", userId);
            Ferramenta ferramenta = new Ferramenta(); ferramenta.setNome(tag);
            ferramenta.setQuantidadeTotal(1); ferramenta.setQuantidadeDisponivel(1);
            ferramenta.setValorDiaria(new BigDecimal("5.00"));
            toolId = ferramentas.saveAndFlush(ferramenta).getIdFerramenta();
            for (int i = 0; i < 2; i++) {
                Kit kit = new Kit(); kit.setNome(tag + "-" + i); kit.setValorDiaria(new BigDecimal("5.00"));
                Long id = kits.saveAndFlush(kit).getIdKit(); kitIds.add(id);
                KitFerramenta c = new KitFerramenta(); c.setIdKit(id); c.setIdFerramenta(toolId); c.setQuantidade(1);
                composicoes.saveAndFlush(c);
            }
            final Long dono = userId;
            final Long ferramentaId = toolId;
            final String email = usuario.getEmail();
            LocalDateTime start = LocalDateTime.of(2099, 1, 1, 10, 0);
            autenticar(email);
            assertThatThrownBy(() -> reservas.reservar(new ReservarKitRequest(dono, kitIds.getFirst(), 2, start, start.plusDays(2))))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(aluguelRepository.findByIdUsuarioOrderByDataCadastroDesc(dono)).isEmpty();

            CountDownLatch ready = new CountDownLatch(2), go = new CountDownLatch(1);
            List<Future<Boolean>> results = new ArrayList<>();
            for (int worker = 0; worker < 2; worker++) {
                final int operacao = worker;
                final Long id = kitIds.get(worker);
                results.add(executor.submit(() -> {
                    autenticar(email); ready.countDown();
                    try {
                        if (!go.await(15, TimeUnit.SECONDS)) throw new IllegalStateException("Barreira de teste expirou");
                        if (operacao == 1 && cenario == 1) {
                            manutencao.atualizar(ferramentaId, new AtualizarFerramentaRequest(tag, null, 1,
                                    new BigDecimal("5.00"), "MANUTENCAO"));
                        } else if (operacao == 1 && cenario == 2) {
                            catalogo.atualizar(kitIds.getFirst(), new AtualizarKitRequest(tag + "-0", null,
                                    new BigDecimal("5.00"), "INATIVO"));
                        } else reservas.reservar(new ReservarKitRequest(dono, id, 1, start, start.plusDays(2)));
                        return true;
                    } catch (IllegalArgumentException expected) { return false; }
                    finally { SecurityContextHolder.clearContext(); }
                }));
            }
            assertThat(ready.await(15, TimeUnit.SECONDS)).isTrue(); go.countDown();
            int successes = 0;
            for (var result : results) if (result.get(30, TimeUnit.SECONDS)) successes++;
            assertThat(successes).isEqualTo(1);
            var persisted = aluguelRepository.findByIdUsuarioOrderByDataCadastroDesc(dono);
            if (cenario != 0 && persisted.isEmpty()) {
                if (cenario == 1) assertThat(ferramentas.findById(ferramentaId).orElseThrow().getStatus()).isEqualTo("MANUTENCAO");
                else assertThat(kits.findById(kitIds.getFirst()).orElseThrow().getStatus()).isEqualTo("INATIVO");
                return;
            }
            assertThat(persisted).hasSize(1);
            assertThat(persisted.getFirst().getValorTotal()).isEqualByComparingTo("10.00");
            assertThat(ferramentas.findById(ferramentaId).orElseThrow().getStatus()).isEqualTo("DISPONIVEL");
            assertThatThrownBy(() -> manutencao.atualizar(ferramentaId,
                    new AtualizarFerramentaRequest(tag, null, 1, new BigDecimal("5.00"), "MANUTENCAO")))
                    .isInstanceOf(IllegalArgumentException.class);
            alugueis.cancelar(persisted.getFirst().getIdAluguel());
            assertThat(aluguelRepository.findById(persisted.getFirst().getIdAluguel()).orElseThrow().getStatus())
                    .isEqualTo("CANCELADO");
        } finally {
            executor.shutdownNow();
            boolean stopped = executor.awaitTermination(30, TimeUnit.SECONDS);
            SecurityContextHolder.clearContext();
            if (!stopped) throw new IllegalStateException("Threads ainda ativas; preservar fixtures para inspeção: " + tag);
            // Nunca limpa tabelas: somente IDs gerados por esta execução.
            if (userId != null) {
                jdbc.update("delete i from aluguel_item i join aluguel a on a.id_aluguel=i.id_aluguel where a.id_usuario=?", userId);
                jdbc.update("delete from aluguel where id_usuario=?", userId);
            }
            for (Long id : kitIds) {
                jdbc.update("delete from kit_ferramenta where id_kit=?", id);
                jdbc.update("delete from kit where id_kit=?", id);
            }
            if (toolId != null) jdbc.update("delete from ferramenta where id_ferramenta=?", toolId);
            if (userId != null) {
                jdbc.update("delete from usuario_perfil where id_usuario=?", userId);
                jdbc.update("delete from usuario where id_usuario=?", userId);
            }
        }
    }
}
