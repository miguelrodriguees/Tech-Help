package br.com.techhelp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Carrega os perfis reais, sem iniciar servidor, JPA ou conectar ao banco. */
class HospedagemConfiguracaoTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    void hospedagemUsaCredenciaisExternasEConfiguracaoDeHttps() {
        runner.withPropertyValues("spring.profiles.active=hospedagem",
                "TECHHELP_DB_URL=jdbc:mariadb://db.example:3306/tech_help_remoto",
                "TECHHELP_DB_USERNAME=techhelp_app", "TECHHELP_DB_PASSWORD=senha-ficticia",
                "TECHHELP_PORT=8181").run(context -> {
            var env = context.getEnvironment();
            assertThat(env.getRequiredProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:mariadb://db.example:3306/tech_help_remoto");
            assertThat(env.getRequiredProperty("spring.datasource.username")).isEqualTo("techhelp_app");
            assertThat(env.getRequiredProperty("spring.datasource.password")).isEqualTo("senha-ficticia");
            assertThat(env.getRequiredProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
            assertThat(env.getRequiredProperty("server.address")).isEqualTo("127.0.0.1");
            assertThat(env.getRequiredProperty("server.port")).isEqualTo("8181");
            assertThat(env.getRequiredProperty("server.forward-headers-strategy")).isEqualTo("framework");
            assertThat(env.getRequiredProperty("server.servlet.session.cookie.secure")).isEqualTo("true");
            assertThat(env.getRequiredProperty("server.servlet.session.cookie.http-only")).isEqualTo("true");
            assertThat(env.getRequiredProperty("spring.jpa.show-sql")).isEqualTo("false");
        });
    }

    @Test void hospedagemNaoUsaUrlLocalSeVariavelAusente() {
        verificarAusencia("spring.datasource.url", "TECHHELP_DB_URL");
    }

    @Test void hospedagemNaoUsaRootSeVariavelAusente() {
        verificarAusencia("spring.datasource.username", "TECHHELP_DB_USERNAME");
    }

    @Test void hospedagemNaoUsaSenhaVaziaSeVariavelAusente() {
        verificarAusencia("spring.datasource.password", "TECHHELP_DB_PASSWORD");
    }

    private void verificarAusencia(String propriedade, String variavel) {
        runner.withPropertyValues("spring.profiles.active=hospedagem").run(context -> {
            assertThatThrownBy(() -> context.getEnvironment().getRequiredProperty(propriedade))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining(variavel);
        });
    }

    @Test void perfilPadraoPreservaDesenvolvimentoLocal() {
        runner.run(context -> {
            var env = context.getEnvironment();
            assertThat(env.getRequiredProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:mariadb://127.0.0.1:3306/tech_help");
            assertThat(env.getRequiredProperty("spring.datasource.username")).isEqualTo("root");
            assertThat(env.getRequiredProperty("spring.datasource.password")).isEmpty();
            assertThat(env.getRequiredProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        });
    }
}
