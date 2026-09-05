package br.com.techhelp.repository;

import br.com.techhelp.model.TecnicoEspecialidade;
import br.com.techhelp.model.TecnicoEspecialidadeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TecnicoEspecialidadeRepository
        extends JpaRepository<TecnicoEspecialidade, TecnicoEspecialidadeId> {

    List<TecnicoEspecialidade> findByIdTecnico(Long idTecnico);

    boolean existsByIdTecnicoAndIdEspecialidade(
            Long idTecnico,
            Long idEspecialidade
    );
}