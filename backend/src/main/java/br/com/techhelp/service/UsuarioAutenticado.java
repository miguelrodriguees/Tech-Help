package br.com.techhelp.service;

import br.com.techhelp.repository.*;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioAutenticado {
    private final UsuarioRepository usuarios;
    private final PerfilRepository perfis;
    private final UsuarioPerfilRepository vinculos;
    public UsuarioAutenticado(UsuarioRepository usuarios, PerfilRepository perfis, UsuarioPerfilRepository vinculos) {
        this.usuarios=usuarios; this.perfis=perfis; this.vinculos=vinculos;
    }
    public Long id(Principal principal) {
        if (principal==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return usuarios.findByEmail(principal.getName()).filter(u->"ATIVO".equals(u.getStatus()))
            .orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getIdUsuario();
    }
    public boolean admin(Long id) {
        return perfis.findByNome("ADMIN").map(p->vinculos.existsByIdUsuarioAndIdPerfil(id,p.getIdPerfil())).orElse(false);
    }
    public Long exigirAdmin(Principal principal) {
        Long id=id(principal);
        if (!admin(id)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Área exclusiva do responsável pelo TechHelp");
        return id;
    }
}
