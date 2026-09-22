package br.com.techhelp.controller;

import br.com.techhelp.repository.UsuarioRepository;
import br.com.techhelp.repository.ClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final br.com.techhelp.repository.TecnicoRepository tecnicos;
    private final br.com.techhelp.service.UsuarioAutenticado sessao;
    public AuthController(UsuarioRepository usuarios, ClienteRepository clientes, br.com.techhelp.repository.TecnicoRepository tecnicos, br.com.techhelp.service.UsuarioAutenticado sessao) {
        this.sessao=sessao;
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.tecnicos = tecnicos;
    }
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }
    public record ContaResponse(Long idUsuario, Long idCliente, Long idTecnico, String nome, String email, boolean administrador) {}
    @GetMapping("/me")
    public ContaResponse me(Principal principal) {
        var usuario = usuarios.findByEmail(principal.getName())
                .filter(u -> "ATIVO".equals(u.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Long idCliente = clientes.findByIdUsuario(usuario.getIdUsuario())
                .map(c -> c.getIdCliente()).orElse(null);
        return new ContaResponse(usuario.getIdUsuario(), idCliente, tecnicos.findByIdUsuario(usuario.getIdUsuario()).map(t -> t.getIdTecnico()).orElse(null), usuario.getNome(), usuario.getEmail(), sessao.admin(usuario.getIdUsuario()));
    }
}
