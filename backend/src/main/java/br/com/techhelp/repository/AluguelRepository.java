package br.com.techhelp.repository;

import br.com.techhelp.model.Aluguel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AluguelRepository
        extends JpaRepository<Aluguel, Long> {

    List<Aluguel> findByIdUsuarioOrderByDataCadastroDesc(
            Long idUsuario
    );
}