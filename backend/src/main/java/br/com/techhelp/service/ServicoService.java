package br.com.techhelp.service;

import br.com.techhelp.model.Proposta;
import br.com.techhelp.model.Servico;
import br.com.techhelp.model.Solicitacao;

import br.com.techhelp.repository.ClienteRepository;
import br.com.techhelp.repository.PropostaRepository;
import br.com.techhelp.repository.ServicoRepository;
import br.com.techhelp.repository.SolicitacaoRepository;
import br.com.techhelp.repository.TecnicoRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final PropostaRepository propostaRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final ClienteRepository clienteRepository;
    private final TecnicoRepository tecnicoRepository;

    public ServicoService(
            ServicoRepository servicoRepository,
            PropostaRepository propostaRepository,
            SolicitacaoRepository solicitacaoRepository,
            ClienteRepository clienteRepository,
            TecnicoRepository tecnicoRepository
    ) {
        this.servicoRepository = servicoRepository;
        this.propostaRepository = propostaRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.clienteRepository = clienteRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    @Transactional
    public Servico aceitarProposta(
            Long idProposta, Long idCliente
    ) {

        Proposta proposta =
                propostaRepository
                        .findById(idProposta)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Proposta não encontrada"
                                )
                        );

        // Mesma trava usada no envio: apenas uma proposta pode vencer por pedido.
        Solicitacao solicitacao = solicitacaoRepository.buscarParaProposta(proposta.getIdSolicitacao())
                .orElseThrow(() -> new NoSuchElementException("Solicitação não encontrada"));
        if (!solicitacao.getIdCliente().equals(idCliente)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
        }
        var existente = servicoRepository.findByIdProposta(idProposta);
        if (existente.isPresent()) return existente.get(); // Repetição após falha de rede.
        if (!List.of("ABERTA", "EM_NEGOCIACAO").contains(solicitacao.getStatus())
                || !"ENVIADA".equals(proposta.getStatus())) {
            throw new IllegalArgumentException("Este pedido ou proposta não permite contratação");
        }

        proposta.setStatus(
                "ACEITA"
        );

        propostaRepository.save(
                proposta
        );

        List<Proposta> outrasPropostas =
                propostaRepository
                        .findByIdSolicitacaoAndStatus(
                                solicitacao.getIdSolicitacao(),
                                "ENVIADA"
                        );

        for (Proposta outra : outrasPropostas) {

            if (!outra.getIdProposta()
                    .equals(idProposta)) {

                outra.setStatus(
                        "RECUSADA"
                );

                propostaRepository.save(
                        outra
                );
            }
        }

        solicitacao.setStatus(
                "CONTRATADA"
        );

        solicitacaoRepository.save(
                solicitacao
        );

        Servico servico =
                new Servico();

        servico.setIdProposta(
                proposta.getIdProposta()
        );

        servico.setIdCliente(
                solicitacao.getIdCliente()
        );

        servico.setIdTecnico(
                proposta.getIdTecnico()
        );

        servico.setValorAcordado(
                proposta.getValor()
        );

        servico.setStatus(
                "AGENDADO"
        );

        return servicoRepository.save(
                servico
        );
    }

    public Servico buscarPorId(
            Long idServico
    ) {

        return servicoRepository
                .findById(idServico)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Serviço não encontrado"
                        )
                );
    }

    @Transactional
    public Servico iniciarServico(
            Long idServico
    ) {

        Servico servico =
                buscarPorId(idServico);

        if (!"AGENDADO".equals(
                servico.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Somente serviços agendados podem ser iniciados"
            );
        }

        servico.setStatus(
                "EM_ANDAMENTO"
        );

        servico.setDataInicio(
                LocalDateTime.now()
        );

        return servicoRepository.save(
                servico
        );
    }

    @Transactional
    public Servico concluirServico(
            Long idServico
    ) {

        Servico servico =
                buscarPorId(idServico);

        if (!"EM_ANDAMENTO".equals(
                servico.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Somente serviços em andamento podem ser concluídos"
            );
        }

        Proposta proposta =
                propostaRepository
                        .findById(
                                servico.getIdProposta()
                        )
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Proposta não encontrada"
                                )
                        );

        Solicitacao solicitacao =
                solicitacaoRepository
                        .findById(
                                proposta.getIdSolicitacao()
                        )
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Solicitação não encontrada"
                                )
                        );

        servico.setStatus(
                "CONCLUIDO"
        );

        servico.setDataConclusao(
                LocalDateTime.now()
        );

        solicitacao.setStatus(
                "CONCLUIDA"
        );

        solicitacaoRepository.save(
                solicitacao
        );

        return servicoRepository.save(
                servico
        );
    }

    public List<Servico> listarPorCliente(
            Long idCliente
    ) {

        if (!clienteRepository.existsById(
                idCliente
        )) {

            throw new NoSuchElementException(
                    "Cliente não encontrado"
            );
        }

        return servicoRepository
                .findByIdClienteOrderByDataCadastroDesc(
                        idCliente
                );
    }

    public List<Servico> listarPorTecnico(
            Long idTecnico
    ) {

        if (!tecnicoRepository.existsById(
                idTecnico
        )) {

            throw new NoSuchElementException(
                    "Técnico não encontrado"
            );
        }

        return servicoRepository
                .findByIdTecnicoOrderByDataCadastroDesc(
                        idTecnico
                );
    }
}