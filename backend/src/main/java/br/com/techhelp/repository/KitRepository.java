package br.com.techhelp.repository;

import br.com.techhelp.model.Kit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KitRepository
        extends JpaRepository<Kit, Long> {

    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdKitNot(String nome, Long idKit);

    List<Kit> findByStatusOrderByNomeAsc(
            String status
    );
}
