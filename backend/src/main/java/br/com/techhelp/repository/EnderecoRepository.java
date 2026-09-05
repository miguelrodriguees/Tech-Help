package br.com.techhelp.repository;

import br.com.techhelp.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnderecoRepository
        extends JpaRepository<Endereco, Long> {

    List<Endereco> findByIdUsuarioOrderByPrincipalDescDataCadastroDesc(
            Long idUsuario
    );
}