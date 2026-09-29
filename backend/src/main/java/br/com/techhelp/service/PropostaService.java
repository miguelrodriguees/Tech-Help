package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.dto.CriarPropostaRequest;
import br.com.techhelp.model.Proposta;
import br.com.techhelp.model.Solicitacao;
import br.com.techhelp.repository.PropostaRepository;
import br.com.techhelp.repository.SolicitacaoRepository;
import br.com.techhelp.repository.TecnicoRepository;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

@Service
@PreAuthorize("@acesso.admin()")
public class PropostaService {

    private final PropostaRepository propostaRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final TecnicoRepository tecnicoRepository;

    public PropostaService(
            PropostaRepository propostaRepository,
            SolicitacaoRepository solicitacaoRepository,
            TecnicoRepository tecnicoRepository
    ) {
        this.propostaRepository = propostaRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    @PreAuthorize("@acesso.tecnico(#dados.idTecnico())")
    public Proposta criar(CriarPropostaRequest dados) {

        Solicitacao solicitacao = solicitacaoRepository
                .findById(dados.idSolicitacao())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Solicitação não encontrada"
                        )
                );

        if (!tecnicoRepository.existsById(
                dados.idTecnico()
        )) {
            throw new NoSuchElementException(
                    "Técnico não encontrado"
            );
        }

        if (!List.of(
                "ABERTA",
                "EM_NEGOCIACAO"
        ).contains(solicitacao.getStatus())) {

            throw new IllegalArgumentException(
                    "Esta solicitação não aceita novas propostas"
            );
        }

        boolean jaExiste =
                propostaRepository
                        .existsByIdSolicitacaoAndIdTecnico(
                                dados.idSolicitacao(),
                                dados.idTecnico()
                        );

        if (jaExiste) {
            throw new IllegalArgumentException(
                    "Este técnico já enviou uma proposta"
            );
        }

        Proposta proposta = new Proposta();

        proposta.setIdSolicitacao(
                dados.idSolicitacao()
        );

        proposta.setIdTecnico(
                dados.idTecnico()
        );

        proposta.setValor(
                dados.valor()
        );

        proposta.setMensagem(
                dados.mensagem()
        );

        proposta.setPrazoEstimadoDias(
                dados.prazoEstimadoDias()
        );

        proposta.setDataDisponivel(
                dados.dataDisponivel()
        );

        proposta.setStatus(
                "ENVIADA"
        );

        Proposta salva =
                propostaRepository.save(proposta);

        if ("ABERTA".equals(
                solicitacao.getStatus()
        )) {

            solicitacao.setStatus(
                    "EM_NEGOCIACAO"
            );

            solicitacaoRepository.save(
                    solicitacao
            );
        }

        return salva;
    }

    @PreAuthorize("@acesso.donoSolicitacao(#idSolicitacao)")
    public List<Proposta> listarPorSolicitacao(
            Long idSolicitacao
    ) {

        if (!solicitacaoRepository.existsById(
                idSolicitacao
        )) {
            throw new NoSuchElementException(
                    "Solicitação não encontrada"
            );
        }

        return propostaRepository
                .findByIdSolicitacaoOrderByDataCadastroDesc(
                        idSolicitacao
                );
    }

    @PreAuthorize("@acesso.tecnico(#idTecnico)")
    public List<Proposta> listarPorTecnico(
            Long idTecnico
    ) {

        if (!tecnicoRepository.existsById(
                idTecnico
        )) {
            throw new NoSuchElementException(
                    "Técnico não encontrado"
            );
        }

        return propostaRepository
                .findByIdTecnicoOrderByDataCadastroDesc(
                        idTecnico
                );
    }
}
