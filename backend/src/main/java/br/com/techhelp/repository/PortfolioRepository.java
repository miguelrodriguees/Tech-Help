package br.com.techhelp.repository;

import br.com.techhelp.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository
        extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByIdTecnicoOrderByDataCadastroDesc(
            Long idTecnico
    );
}