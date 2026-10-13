package br.com.techhelp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Contexto real, somente leitura; confirma cabeçalhos de proxy no perfil de demonstração. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demonstracao")
@EnabledIfSystemProperty(named = "techhelp.demo.integration", matches = "true")
class DemonstracaoProxyTests {
    @Autowired MockMvc mvc;

    @Test void httpsEncaminhadoNaoBloqueiaRecursosDaMesmaOrigem() throws Exception {
        mvc.perform(get("/assets/inexistente.js")
                .header("Host", "demo.example")
                .header("X-Forwarded-Proto", "https")
                .header("Origin", "https://demo.example"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/auth/me").header("Host", "demo.example")
                .header("X-Forwarded-Proto", "https").header("Origin", "https://demo.example"))
                .andExpect(status().isUnauthorized());
        mvc.perform(options("/auth/login").header("Host", "demo.example")
                .header("X-Forwarded-Proto", "https").header("Origin", "https://outro.example")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
