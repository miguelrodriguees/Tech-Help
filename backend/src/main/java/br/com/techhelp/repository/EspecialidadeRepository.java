package br.com.techhelp.repository;

import br.com.techhelp.model.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EspecialidadeRepository
        extends JpaRepository<Especialidade, Long> {

    List<Especialidade> findByAtivoTrueOrderByNomeAsc();

    List<Especialidade> findByIdCategoriaAndAtivoTrueOrderByNomeAsc(
            Long idCategoria
    );
}