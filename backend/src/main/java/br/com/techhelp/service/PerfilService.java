package br.com.techhelp.service;

import br.com.techhelp.dto.VincularPerfilRequest;
import br.com.techhelp.model.Perfil;
import br.com.techhelp.model.UsuarioPerfil;
import br.com.techhelp.model.UsuarioPerfilId;

import br.com.techhelp.repository.PerfilRepository;
import br.com.techhelp.repository.UsuarioPerfilRepository;
import br.com.techhelp.repository.UsuarioRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final UsuarioPerfilRepository usuarioPerfilRepository;
    private final UsuarioRepository usuarioRepository;

    public PerfilService(
            PerfilRepository perfilRepository,
            UsuarioPerfilRepository usuarioPerfilRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.perfilRepository = perfilRepository;
        this.usuarioPerfilRepository =
                usuarioPerfilRepository;
        this.usuarioRepository =
                usuarioRepository;
    }

    public List<Perfil> listarPerfis() {

        return perfilRepository.findAll();
    }

    public UsuarioPerfil vincular(
            VincularPerfilRequest request
    ) {

        if (!usuarioRepository.existsById(
                request.idUsuario()
        )) {

            throw new NoSuchElementException(
                    "Usuário não encontrado"
            );
        }

        if (!perfilRepository.existsById(
                request.idPerfil()
        )) {

            throw new NoSuchElementException(
                    "Perfil não encontrado"
            );
        }

        if (usuarioPerfilRepository
                .existsByIdUsuarioAndIdPerfil(
                        request.idUsuario(),
                        request.idPerfil()
                )) {

            throw new IllegalArgumentException(
                    "Usuário já possui este perfil"
            );
        }

        UsuarioPerfil vinculo =
                new UsuarioPerfil();

        vinculo.setIdUsuario(
                request.idUsuario()
        );

        vinculo.setIdPerfil(
                request.idPerfil()
        );

        return usuarioPerfilRepository.save(
                vinculo
        );
    }

    public List<UsuarioPerfil> listarPorUsuario(
            Long idUsuario
    ) {

        if (!usuarioRepository.existsById(
                idUsuario
        )) {

            throw new NoSuchElementException(
                    "Usuário não encontrado"
            );
        }

        return usuarioPerfilRepository
                .findByIdUsuario(idUsuario);
    }

    public void remover(
            Long idUsuario,
            Long idPerfil
    ) {

        UsuarioPerfilId id =
                new UsuarioPerfilId(
                        idUsuario,
                        idPerfil
                );

        if (!usuarioPerfilRepository
                .existsById(id)) {

            throw new NoSuchElementException(
                    "Vínculo de perfil não encontrado"
            );
        }

        usuarioPerfilRepository
                .deleteById(id);
    }
}