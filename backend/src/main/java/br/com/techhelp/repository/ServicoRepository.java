package br.com.techhelp.repository;

import br.com.techhelp.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicoRepository
        extends JpaRepository<Servico, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select s from Servico s where s.idServico = :id")
    java.util.Optional<Servico> buscarParaAtualizar(@org.springframework.data.repository.query.Param("id") Long id);

    java.util.Optional<Servico> findByIdProposta(Long idProposta);

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