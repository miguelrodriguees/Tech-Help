package br.com.techhelp.repository;

import br.com.techhelp.model.Ferramenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FerramentaRepository
        extends JpaRepository<Ferramenta, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    List<Ferramenta>
    findByStatusAndQuantidadeDisponivelGreaterThanOrderByNomeAsc(
            String status,
            Integer quantidade
    );
}