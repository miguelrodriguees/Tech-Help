package br.com.techhelp.repository;

import br.com.techhelp.model.Proposta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropostaRepository
        extends JpaRepository<Proposta, Long> {

    boolean existsByIdSolicitacaoAndIdTecnico(
            Long idSolicitacao,
            Long idTecnico
    );

    List<Proposta> findByIdSolicitacaoOrderByDataCadastroDesc(
            Long idSolicitacao
    );

    List<Proposta> findByIdSolicitacaoAndStatus(
            Long idSolicitacao,
            String status
    );

    List<Proposta> findByIdTecnicoOrderByDataCadastroDesc(
            Long idTecnico
    );
}