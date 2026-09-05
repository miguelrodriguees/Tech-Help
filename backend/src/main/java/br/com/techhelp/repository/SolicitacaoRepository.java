package br.com.techhelp.repository;

import br.com.techhelp.model.Solicitacao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoRepository
        extends JpaRepository<Solicitacao, Long> {

    List<Solicitacao> findByStatusOrderByDataCadastroDesc(String status);

    List<Solicitacao> findByIdClienteOrderByDataCadastroDesc(Long idCliente);
}
