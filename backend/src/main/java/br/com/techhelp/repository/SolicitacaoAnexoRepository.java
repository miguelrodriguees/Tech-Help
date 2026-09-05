package br.com.techhelp.repository;

import br.com.techhelp.model.SolicitacaoAnexo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoAnexoRepository
        extends JpaRepository<SolicitacaoAnexo, Long> {

    List<SolicitacaoAnexo>
    findByIdSolicitacaoOrderByDataUploadDesc(
            Long idSolicitacao
    );
}