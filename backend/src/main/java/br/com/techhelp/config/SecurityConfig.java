package br.com.techhelp.config;

import br.com.techhelp.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import java.util.Locale;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return email -> {
            var usuario = usuarios.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                    .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
            return User.withUsername(usuario.getEmail()).password(usuario.getSenhaHash())
                    .authorities("USUARIO").disabled(!"ATIVO".equals(usuario.getStatus())).build();
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF permanece ativo, inclusive no cadastro, login e logout.
        http.cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/categorias", "/auth/csrf").permitAll()
                .requestMatchers(HttpMethod.POST, "/cadastro/cliente", "/cadastro/tecnico", "/auth/login").permitAll()
                .requestMatchers("/auth/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/tecnico/solicitacoes", "/propostas/minhas").authenticated()
                .requestMatchers(HttpMethod.POST, "/propostas", "/servicos/aceitar-proposta/*").authenticated()
                .requestMatchers(HttpMethod.GET, "/cliente/solicitacoes/*/propostas", "/servicos/meus").authenticated()
                .requestMatchers(HttpMethod.POST, "/solicitacoes").authenticated()
                .requestMatchers(HttpMethod.GET, "/solicitacoes/minhas").authenticated()
                .requestMatchers(HttpMethod.GET, "/atendimentos/tecnico", "/atendimentos/*/avaliacoes/cliente", "/atendimentos/*/avaliacoes/tecnico").authenticated()
                .requestMatchers(HttpMethod.POST, "/atendimentos/*/iniciar", "/atendimentos/*/concluir", "/atendimentos/*/avaliacao").authenticated()
                // Liberar cada fluxo apenas junto com sua regra de propriedade dos dados.
                .anyRequest().denyAll())
            .requestCache(cache -> cache.disable())
            .formLogin(form -> form.loginProcessingUrl("/auth/login")
                .usernameParameter("email").passwordParameter("senha")
                .successHandler((request, response, authentication) -> response.setStatus(204))
                .failureHandler((request, response, exception) -> response.setStatus(401)))
            .logout(logout -> logout.logoutUrl("/auth/logout")
                .invalidateHttpSession(true).deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)))
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, exception) -> response.setStatus(401))
                .accessDeniedHandler((request, response, exception) -> response.setStatus(403)));
        return http.build();
    }
}
