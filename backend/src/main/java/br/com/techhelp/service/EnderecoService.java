package br.com.techhelp.service;

import br.com.techhelp.dto.CriarEnderecoRequest;
import br.com.techhelp.model.Endereco;
import br.com.techhelp.repository.EnderecoRepository;
import br.com.techhelp.repository.UsuarioRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final UsuarioRepository usuarioRepository;

    public EnderecoService(
            EnderecoRepository enderecoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.enderecoRepository = enderecoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Endereco criar(
            CriarEnderecoRequest request
    ) {

        if (!usuarioRepository.existsById(
                request.idUsuario()
        )) {
            throw new NoSuchElementException(
                    "Usuário não encontrado"
            );
        }

        Endereco endereco = new Endereco();

        endereco.setIdUsuario(request.idUsuario());
        endereco.setApelido(request.apelido());
        endereco.setCep(request.cep());
        endereco.setLogradouro(request.logradouro());
        endereco.setNumero(request.numero());
        endereco.setComplemento(request.complemento());
        endereco.setBairro(request.bairro());
        endereco.setCidade(request.cidade());
        endereco.setEstado(request.estado());

        endereco.setPrincipal(
                request.principal() != null
                        ? request.principal()
                        : false
        );

        return enderecoRepository.save(endereco);
    }

    public Endereco buscarPorId(
            Long idEndereco
    ) {

        return enderecoRepository
                .findById(idEndereco)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Endereço não encontrado"
                        ));
    }

    public List<Endereco> listarPorUsuario(
            Long idUsuario
    ) {

        if (!usuarioRepository.existsById(idUsuario)) {
            throw new NoSuchElementException(
                    "Usuário não encontrado"
            );
        }

        return enderecoRepository
                .findByIdUsuarioOrderByPrincipalDescDataCadastroDesc(
                        idUsuario
                );
    }

    public Endereco atualizar(
            Long idEndereco,
            CriarEnderecoRequest request
    ) {

        Endereco endereco =
                buscarPorId(idEndereco);

        if (!endereco.getIdUsuario()
                .equals(request.idUsuario())) {

            throw new IllegalArgumentException(
                    "O endereço não pertence a este usuário"
            );
        }

        endereco.setApelido(request.apelido());
        endereco.setCep(request.cep());
        endereco.setLogradouro(request.logradouro());
        endereco.setNumero(request.numero());
        endereco.setComplemento(request.complemento());
        endereco.setBairro(request.bairro());
        endereco.setCidade(request.cidade());
        endereco.setEstado(request.estado());

        endereco.setPrincipal(
                request.principal() != null
                        ? request.principal()
                        : false
        );

        return enderecoRepository.save(endereco);
    }

    public void excluir(
            Long idEndereco
    ) {

        Endereco endereco =
                buscarPorId(idEndereco);

        enderecoRepository.delete(endereco);
    }
}