package br.com.techhelp.service;

import br.com.techhelp.dto.CadastrarClienteRequest;
import br.com.techhelp.dto.CadastrarTecnicoRequest;
import br.com.techhelp.dto.CadastroResponse;
import br.com.techhelp.model.Cliente;
import br.com.techhelp.model.Perfil;
import br.com.techhelp.model.Tecnico;
import br.com.techhelp.model.Usuario;
import br.com.techhelp.model.UsuarioPerfil;
import br.com.techhelp.repository.ClienteRepository;
import br.com.techhelp.repository.PerfilRepository;
import br.com.techhelp.repository.TecnicoRepository;
import br.com.techhelp.repository.UsuarioPerfilRepository;
import br.com.techhelp.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class CadastroService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final TecnicoRepository tecnicoRepository;
    private final PerfilRepository perfilRepository;
    private final UsuarioPerfilRepository usuarioPerfilRepository;

    private final PasswordEncoder passwordEncoder;

    public CadastroService(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            TecnicoRepository tecnicoRepository,
            PerfilRepository perfilRepository,
            UsuarioPerfilRepository usuarioPerfilRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.perfilRepository = perfilRepository;
        this.usuarioPerfilRepository =
                usuarioPerfilRepository;
    }

    @Transactional
    public CadastroResponse cadastrarCliente(
            CadastrarClienteRequest request
    ) {

        validarUsuarioNovo(
                request.email(),
                request.cpf()
        );

        Usuario usuario = criarUsuario(
                request.nome(),
                request.email(),
                request.senha(),
                request.telefone(),
                request.cpf()
        );

        Cliente cliente = new Cliente();

        cliente.setIdUsuario(
                usuario.getIdUsuario()
        );

        cliente = clienteRepository.save(cliente);

        Perfil perfil =
                buscarPerfil("CLIENTE");

        vincularPerfil(
                usuario.getIdUsuario(),
                perfil.getIdPerfil()
        );

        return new CadastroResponse(
                usuario.getIdUsuario(),
                cliente.getIdCliente(),
                null,
                usuario.getNome(),
                usuario.getEmail(),
                perfil.getNome()
        );
    }

    @Transactional
    public CadastroResponse cadastrarTecnico(
            CadastrarTecnicoRequest request
    ) {

        validarUsuarioNovo(
                request.email(),
                request.cpf()
        );

        Usuario usuario = criarUsuario(
                request.nome(),
                request.email(),
                request.senha(),
                request.telefone(),
                request.cpf()
        );

        Tecnico tecnico = new Tecnico();

        tecnico.setIdUsuario(
                usuario.getIdUsuario()
        );

        tecnico.setDescricao(
                request.descricao()
        );

        tecnico.setAnosExperiencia(
                request.anosExperiencia()
        );

        tecnico.setStatusVerificacao(
                "PENDENTE"
        );

        tecnico = tecnicoRepository.save(tecnico);

        Perfil perfil =
                buscarPerfil("TECNICO");

        vincularPerfil(
                usuario.getIdUsuario(),
                perfil.getIdPerfil()
        );

        return new CadastroResponse(
                usuario.getIdUsuario(),
                null,
                tecnico.getIdTecnico(),
                usuario.getNome(),
                usuario.getEmail(),
                perfil.getNome()
        );
    }

    private Usuario criarUsuario(
            String nome,
            String email,
            String senha,
            String telefone,
            String cpf
    ) {

        // BCrypt limita a senha por bytes; acentos podem ocupar mais de um byte.
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("A senha excede o limite permitido. Use uma senha menor.");
        }
        Usuario usuario = new Usuario();

        usuario.setNome(
                nome.trim()
        );

        usuario.setEmail(
                email.trim().toLowerCase(Locale.ROOT)
        );

        usuario.setSenhaHash(
                passwordEncoder.encode(senha)
        );

        if (telefone == null
                || telefone.isBlank()) {

            usuario.setTelefone(null);

        } else {

            usuario.setTelefone(
                    telefone.trim()
            );
        }

        usuario.setCpf(cpf);

        usuario.setStatus("ATIVO");

        usuario.setEmailVerificado(false);

        return usuarioRepository.save(usuario);
    }

    private void validarUsuarioNovo(
            String email,
            String cpf
    ) {

        String emailNormalizado =
                email.trim().toLowerCase(Locale.ROOT);

        if (usuarioRepository
                .existsByEmail(emailNormalizado)) {

            throw new IllegalArgumentException(
                    "E-mail já cadastrado"
            );
        }

        if (usuarioRepository
                .existsByCpf(cpf)) {

            throw new IllegalArgumentException(
                    "CPF já cadastrado"
            );
        }
    }

    private Perfil buscarPerfil(
            String nome
    ) {

        return perfilRepository
                .findByNome(nome)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Perfil "
                                + nome
                                + " não encontrado"
                        ));
    }

    private void vincularPerfil(
            Long idUsuario,
            Long idPerfil
    ) {

        UsuarioPerfil vinculo =
                new UsuarioPerfil();

        vinculo.setIdUsuario(idUsuario);

        vinculo.setIdPerfil(idPerfil);

        usuarioPerfilRepository.save(vinculo);
    }
}