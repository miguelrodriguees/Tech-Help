package br.com.techhelp.repository;

import br.com.techhelp.model.Certificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificacaoRepository
        extends JpaRepository<Certificacao, Long> {

    List<Certificacao> findByIdTecnicoOrderByDataCadastroDesc(
            Long idTecnico
    );

    boolean existsByIdTecnicoAndCodigoCredencial(
            Long idTecnico,
            String codigoCredencial
    );
}