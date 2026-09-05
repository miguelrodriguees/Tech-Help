package br.com.techhelp.repository;

import br.com.techhelp.model.Favorito;
import br.com.techhelp.model.FavoritoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoritoRepository
        extends JpaRepository<Favorito, FavoritoId> {

    boolean existsByIdClienteAndIdTecnico(
            Long idCliente,
            Long idTecnico
    );

    List<Favorito> findByIdClienteOrderByDataCadastroDesc(
            Long idCliente
    );
}