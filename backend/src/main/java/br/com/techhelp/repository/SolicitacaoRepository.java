package br.com.techhelp.repository;

import br.com.techhelp.model.Solicitacao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoRepository
        extends JpaRepository<Solicitacao, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select s from Solicitacao s where s.idSolicitacao = :id")
    java.util.Optional<Solicitacao> buscarParaProposta(@org.springframework.data.repository.query.Param("id") Long id);

    List<Solicitacao> findByStatusInOrderByDataCadastroDesc(List<String> status);

    List<Solicitacao> findByStatusOrderByDataCadastroDesc(String status);

    List<Solicitacao> findByIdClienteOrderByDataCadastroDesc(Long idCliente);
}
