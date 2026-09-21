package br.com.techhelp.service;

import br.com.techhelp.repository.UsuarioRepository;
import br.com.techhelp.repository.ClienteRepository;
import java.security.Principal;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClienteAutenticado {
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    public ClienteAutenticado(UsuarioRepository usuarios, ClienteRepository clientes) {
        this.usuarios = usuarios;
        this.clientes = clientes;
    }
    public Long id(Principal principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        var usuario = usuarios.findByEmail(principal.getName())
                .filter(u -> "ATIVO".equals(u.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return clientes.findByIdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "É necessária uma conta de cliente"))
                .getIdCliente();
    }
}
