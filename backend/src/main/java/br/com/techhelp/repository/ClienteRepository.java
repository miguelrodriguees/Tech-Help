package br.com.techhelp.repository;

import br.com.techhelp.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByIdUsuario(Long idUsuario);

    boolean existsByIdUsuario(Long idUsuario);
}