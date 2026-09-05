package br.com.techhelp.repository;

import br.com.techhelp.model.UsuarioPerfil;
import br.com.techhelp.model.UsuarioPerfilId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioPerfilRepository
        extends JpaRepository<UsuarioPerfil, UsuarioPerfilId> {

    List<UsuarioPerfil> findByIdUsuario(
            Long idUsuario
    );

    boolean existsByIdUsuarioAndIdPerfil(
            Long idUsuario,
            Long idPerfil
    );
}