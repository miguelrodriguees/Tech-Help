package br.com.techhelp.repository;

import br.com.techhelp.model.AluguelItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AluguelItemRepository
        extends JpaRepository<AluguelItem, Long> {

    @Query("""
            select coalesce(sum(cast(i.quantidade as long) * c.quantidade), 0)
            from AluguelItem i, Aluguel a, KitFerramenta c
            where a.idAluguel = i.idAluguel and c.idKit = i.idKit
              and c.idFerramenta = :idFerramenta
              and a.status in ('RESERVADO', 'RETIRADO', 'ATRASADO')
            """)
    long quantidadeComprometida(Long idFerramenta);

    @Query("""
            select count(i) from AluguelItem i, Aluguel a
            where a.idAluguel = i.idAluguel and i.idKit = :idKit
              and a.status in ('RESERVADO', 'RETIRADO', 'ATRASADO')
            """)
    long contarReservasAtivasDoKit(Long idKit);

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
