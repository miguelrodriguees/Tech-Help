package br.com.techhelp.repository;

import br.com.techhelp.model.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DenunciaRepository
        extends JpaRepository<Denuncia, Long> {

    List<Denuncia> findByIdDenuncianteOrderByDataCadastroDesc(
            Long idDenunciante
    );

    List<Denuncia> findByIdUsuarioDenunciadoOrderByDataCadastroDesc(
            Long idUsuarioDenunciado
    );

    List<Denuncia> findByStatusOrderByDataCadastroDesc(
            String status
    );
}