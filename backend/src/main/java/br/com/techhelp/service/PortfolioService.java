package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.dto.CriarPortfolioRequest;
import br.com.techhelp.model.Portfolio;
import br.com.techhelp.repository.PortfolioRepository;
import br.com.techhelp.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@PreAuthorize("@acesso.admin()")
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final TecnicoRepository tecnicoRepository;

    public PortfolioService(
            PortfolioRepository portfolioRepository,
            TecnicoRepository tecnicoRepository
    ) {
        this.portfolioRepository = portfolioRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    @PreAuthorize("@acesso.tecnico(#request.idTecnico())")
    public Portfolio criar(CriarPortfolioRequest request) {

        if (!tecnicoRepository.existsById(request.idTecnico())) {
            throw new NoSuchElementException(
                    "Técnico não encontrado"
            );
        }

        Portfolio portfolio = new Portfolio();

        portfolio.setIdTecnico(request.idTecnico());
        portfolio.setTitulo(request.titulo());
        portfolio.setDescricao(request.descricao());
        portfolio.setImagemUrl(request.imagemUrl());
        portfolio.setLinkProjeto(request.linkProjeto());

        return portfolioRepository.save(portfolio);
    }

    @PreAuthorize("permitAll()")
    public List<Portfolio> listarPorTecnico(Long idTecnico) {

        if (!tecnicoRepository.existsById(idTecnico)) {
            throw new NoSuchElementException(
                    "Técnico não encontrado"
            );
        }

        return portfolioRepository
                .findByIdTecnicoOrderByDataCadastroDesc(idTecnico);
    }

    @PreAuthorize("@acesso.portfolio(#idPortfolio)")
    public void excluir(Long idPortfolio) {

        Portfolio portfolio = portfolioRepository
                .findById(idPortfolio)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Item do portfólio não encontrado"
                        ));

        portfolioRepository.delete(portfolio);
    }
}
