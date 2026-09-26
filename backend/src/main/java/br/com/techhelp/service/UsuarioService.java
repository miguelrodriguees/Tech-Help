package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.model.Usuario;
import br.com.techhelp.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("@acesso.admin()")
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PreAuthorize("@acesso.usuario(#id)")
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }
}
