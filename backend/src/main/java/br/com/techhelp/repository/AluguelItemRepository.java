package br.com.techhelp.repository;

import br.com.techhelp.model.AluguelItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AluguelItemRepository
        extends JpaRepository<AluguelItem, Long> {

    List<AluguelItem> findByIdAluguelOrderByIdAluguelItemAsc(
            Long idAluguel
    );

    boolean existsByIdAluguelAndIdKit(
            Long idAluguel,
            Long idKit
    );

    long countByIdAluguel(
            Long idAluguel
    );
}