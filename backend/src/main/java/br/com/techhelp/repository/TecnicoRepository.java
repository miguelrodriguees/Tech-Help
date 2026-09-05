package br.com.techhelp.repository;

import br.com.techhelp.model.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {

    Optional<Tecnico> findByIdUsuario(Long idUsuario);

    boolean existsByIdUsuario(Long idUsuario);
}