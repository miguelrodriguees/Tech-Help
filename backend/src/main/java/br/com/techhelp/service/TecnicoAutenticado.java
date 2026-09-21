package br.com.techhelp.service;

import br.com.techhelp.repository.UsuarioRepository;
import br.com.techhelp.repository.TecnicoRepository;
import java.security.Principal;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TecnicoAutenticado {
    private final UsuarioRepository usuarios;
    private final TecnicoRepository tecnicos;
    public TecnicoAutenticado(UsuarioRepository usuarios, TecnicoRepository tecnicos) {
        this.usuarios = usuarios;
        this.tecnicos = tecnicos;
    }
    public Long id(Principal principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        var usuario = usuarios.findByEmail(principal.getName())
                .filter(u -> "ATIVO".equals(u.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return tecnicos.findByIdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "É necessária uma conta de técnico"))
                .getIdTecnico();
    }
}
