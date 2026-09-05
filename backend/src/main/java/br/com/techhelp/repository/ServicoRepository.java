package br.com.techhelp.repository;

import br.com.techhelp.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicoRepository
        extends JpaRepository<Servico, Long> {

    boolean existsByIdProposta(
            Long idProposta
    );

    List<Servico> findByIdClienteOrderByDataCadastroDesc(
            Long idCliente
    );

    List<Servico> findByIdTecnicoOrderByDataCadastroDesc(
            Long idTecnico
    );
}