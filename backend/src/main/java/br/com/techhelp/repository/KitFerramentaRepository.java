package br.com.techhelp.repository;

import br.com.techhelp.model.KitFerramenta;
import br.com.techhelp.model.KitFerramentaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KitFerramentaRepository
        extends JpaRepository<KitFerramenta, KitFerramentaId> {

    List<KitFerramenta> findByIdKit(
            Long idKit
    );

    boolean existsByIdKitAndIdFerramenta(
            Long idKit,
            Long idFerramenta
    );
}