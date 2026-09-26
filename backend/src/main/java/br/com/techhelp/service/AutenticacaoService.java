package br.com.techhelp.service;

import br.com.techhelp.model.Usuario;
import br.com.techhelp.repository.UsuarioRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {
    private final UsuarioRepository usuarios;

    public AutenticacaoService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Usuario usuario = usuarios.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais invalidas"));
        // Os perfis e o status sao conferidos no banco em cada operacao protegida.
        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities("AUTENTICADO")
                .disabled(!"ATIVO".equals(usuario.getStatus()))
                .build();
    }
}
