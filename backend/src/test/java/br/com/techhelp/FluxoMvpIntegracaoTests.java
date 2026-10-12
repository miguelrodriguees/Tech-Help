package br.com.techhelp;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Percurso HTTP real com banco, usuários e pedido temporários próprios. */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfSystemProperty(named = "techhelp.mvp.integration", matches = "true")
class FluxoMvpIntegracaoTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    private MockHttpSession login(String email, String senha) throws Exception {
        return (MockHttpSession) mvc.perform(post("/auth/login").with(csrf())
                .param("email", email).param("senha", senha))
                .andExpect(status().isNoContent()).andReturn().getRequest().getSession(false);
    }

    @Test void cadastroLoginPedidoPropostaExecucaoEAvaliacao() throws Exception {
        assertThat(jdbc.queryForObject("select database()", String.class))
                .isEqualTo(System.getProperty("techhelp.integration.database"));
        String tag = "TESTE-FLUXO-" + UUID.randomUUID();
        String senha = "Teste!" + UUID.randomUUID();
        Long categoria = null;
        try {
            for (String perfil : List.of("cliente", "tecnico")) {
                String cpf = String.format("%011d", ThreadLocalRandom.current().nextLong(100000000000L));
                mvc.perform(post("/cadastro/" + perfil).with(csrf()).contentType("application/json")
                        .content("""
                        {"nome":"%s","email":"%s","senha":"%s","cpf":"%s","anosExperiencia":1}
                        """.formatted(tag, tag + perfil + "@example.invalid", senha, cpf)))
                        .andExpect(status().isCreated());
            }
            long cliente = jdbc.queryForObject("select c.id_cliente from cliente c join usuario u on u.id_usuario=c.id_usuario where u.email=?", Long.class, tag + "cliente@example.invalid");
            long tecnico = jdbc.queryForObject("select t.id_tecnico from tecnico t join usuario u on u.id_usuario=t.id_usuario where u.email=?", Long.class, tag + "tecnico@example.invalid");
            jdbc.update("insert into categoria(nome) values (?)", tag);
            categoria = jdbc.queryForObject("select id_categoria from categoria where nome=?", Long.class, tag);
            var c = login(tag + "cliente@example.invalid", senha);
            var t = login(tag + "tecnico@example.invalid", senha);
            assertThat(c).isNotNull(); assertThat(t).isNotNull();
            mvc.perform(get("/auth/me").session(c)).andExpect(status().isOk()).andExpect(jsonPath("$.perfis[0]").value("CLIENTE"));
            String pedido = """
                    {"idCliente":%d,"idCategoria":%d,"titulo":"%s","descricao":"Teste temporário de fluxo","tipoAtendimento":"REMOTO"}
                    """.formatted(cliente, categoria, tag);
            mvc.perform(post("/solicitacoes").with(csrf()).contentType("application/json").content(pedido)).andExpect(status().isUnauthorized());
            mvc.perform(post("/solicitacoes").session(c).contentType("application/json").content(pedido)).andExpect(status().isForbidden());
            mvc.perform(post("/solicitacoes").session(c).with(csrf()).contentType("application/json").content(pedido))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.dataCadastro").isNotEmpty());
            long solicitacao = jdbc.queryForObject("select id_solicitacao from solicitacao where titulo=?", Long.class, tag);
            mvc.perform(post("/propostas").session(t).with(csrf()).contentType("application/json")
                    .content("{\"idSolicitacao\":" + solicitacao + ",\"idTecnico\":" + tecnico + ",\"valor\":100}"))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.dataCadastro").isNotEmpty());
            long proposta = jdbc.queryForObject("select id_proposta from proposta where id_solicitacao=?", Long.class, solicitacao);
            mvc.perform(post("/servicos/aceitar-proposta/" + proposta).session(t).with(csrf())).andExpect(status().isForbidden());
            mvc.perform(post("/servicos/aceitar-proposta/" + proposta).session(c).with(csrf()))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.dataCadastro").isNotEmpty());
            long servico = jdbc.queryForObject("select id_servico from servico where id_proposta=?", Long.class, proposta);
            mvc.perform(post("/servicos/" + servico + "/concluir").session(t).with(csrf())).andExpect(status().isBadRequest());
            mvc.perform(post("/servicos/" + servico + "/iniciar").session(c).with(csrf())).andExpect(status().isForbidden());
            mvc.perform(post("/servicos/" + servico + "/iniciar").session(t).with(csrf())).andExpect(status().isOk());
            mvc.perform(post("/servicos/" + servico + "/concluir").session(t).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONCLUIDO"));
            String rota = "/avaliacoes/servico/" + servico + "/cliente";
            mvc.perform(post(rota).session(c).with(csrf()).contentType("application/json").content("{\"nota\":5,\"comentario\":\"Resolvido no teste\"}"))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.dataAvaliacao").isNotEmpty());
            mvc.perform(post(rota).session(c).with(csrf()).contentType("application/json").content("{\"nota\":5}"))
                    .andExpect(status().isBadRequest());
            assertThat(jdbc.queryForObject("select status from solicitacao where id_solicitacao=?", String.class, solicitacao)).isEqualTo("CONCLUIDA");
            assertThat(jdbc.queryForObject("select count(*) from avaliacao where id_servico=?", Integer.class, servico)).isEqualTo(1);
            mvc.perform(post("/auth/logout").session(c).with(csrf())).andExpect(status().isNoContent());
        } finally {
            // Recupera apenas IDs de usuários desta execução, inclusive se uma asserção falhar.
            var ids = jdbc.queryForList("select id_usuario from usuario where nome=?", Long.class, tag);
            for (long user : ids) {
                jdbc.update("delete a from avaliacao a join servico s on s.id_servico=a.id_servico join cliente c on c.id_cliente=s.id_cliente where c.id_usuario=?", user);
                jdbc.update("delete s from servico s join cliente c on c.id_cliente=s.id_cliente where c.id_usuario=?", user);
                jdbc.update("delete p from proposta p join solicitacao r on r.id_solicitacao=p.id_solicitacao join cliente c on c.id_cliente=r.id_cliente where c.id_usuario=?", user);
                jdbc.update("delete r from solicitacao r join cliente c on c.id_cliente=r.id_cliente where c.id_usuario=?", user);
            }
            if (categoria != null) jdbc.update("delete from categoria where id_categoria=?", categoria);
            for (long user : ids) {
                jdbc.update("delete from cliente where id_usuario=?", user);
                jdbc.update("delete from tecnico where id_usuario=?", user);
                jdbc.update("delete from usuario_perfil where id_usuario=?", user);
                jdbc.update("delete from usuario where id_usuario=?", user);
            }
        }
    }
}
