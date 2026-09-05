package br.com.techhelp.repository;

import br.com.techhelp.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvaliacaoRepository
        extends JpaRepository<Avaliacao, Long> {

    boolean existsByIdServicoAndIdAvaliadorAndIdAvaliado(
            Long idServico,
            Long idAvaliador,
            Long idAvaliado
    );

    List<Avaliacao> findByIdAvaliadoOrderByDataAvaliacaoDesc(
            Long idAvaliado
    );

    List<Avaliacao> findByIdServicoOrderByDataAvaliacaoDesc(
            Long idServico
    );
}