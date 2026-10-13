package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.model.Proposta;
import br.com.techhelp.model.Servico;
import br.com.techhelp.model.Solicitacao;

import br.com.techhelp.repository.ClienteRepository;
import br.com.techhelp.repository.PropostaRepository;
import br.com.techhelp.repository.ServicoRepository;
import br.com.techhelp.repository.SolicitacaoRepository;
import br.com.techhelp.repository.TecnicoRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@PreAuthorize("@acesso.admin()")
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final PropostaRepository propostaRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final ClienteRepository clienteRepository;
    private final TecnicoRepository tecnicoRepository;
    private final EntityManager entityManager;

    public ServicoService(
            ServicoRepository servicoRepository,
            PropostaRepository propostaRepository,
            SolicitacaoRepository solicitacaoRepository,
            ClienteRepository clienteRepository,
            TecnicoRepository tecnicoRepository,
            EntityManager entityManager
    ) {
        this.servicoRepository = servicoRepository;
        this.propostaRepository = propostaRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.clienteRepository = clienteRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.entityManager = entityManager;
    }

    private Servico buscarParaAlterar(Long idServico) {
        Servico servico = buscarPorId(idServico);
        // Atualiza também uma entidade já carregada pela autorização antes de validar o estado.
        entityManager.refresh(servico, LockModeType.PESSIMISTIC_WRITE);
        return servico;
    }

    @Transactional
    @PreAuthorize("@acesso.executarServico(#idServico)")
    public Servico agendar(Long idServico, LocalDateTime dataAgendada) {
        Servico servico = buscarParaAlterar(idServico);
        if (!"AGENDADO".equals(servico.getStatus())) {
            throw new IllegalArgumentException("Somente atendimentos ainda não iniciados podem ser agendados");
        }
        if (dataAgendada == null || !dataAgendada.isAfter(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")))) {
            throw new IllegalArgumentException("Informe uma data e hora futuras, no horário de Brasília");
        }
        servico.setDataAgendada(dataAgendada);
        return servicoRepository.save(servico);
    }

    @Transactional
    @PreAuthorize("@acesso.servico(#idServico)")
    public Servico cancelar(Long idServico) {
        Servico servico = buscarParaAlterar(idServico);
        if (!"AGENDADO".equals(servico.getStatus())) {
            throw new IllegalArgumentException("Somente atendimentos ainda não iniciados podem ser cancelados");
        }
        Proposta proposta = propostaRepository.findById(servico.getIdProposta())
                .orElseThrow(() -> new NoSuchElementException("Proposta não encontrada"));
        Solicitacao solicitacao = solicitacaoRepository.findById(proposta.getIdSolicitacao())
                .orElseThrow(() -> new NoSuchElementException("Solicitação não encontrada"));
        servico.setStatus("CANCELADO");
        solicitacao.setStatus("CANCELADA");
        solicitacaoRepository.save(solicitacao);
        return servicoRepository.save(servico);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("@acesso.aceitarProposta(#idProposta)")
    public Servico aceitarProposta(
            Long idProposta
    ) {

        Proposta proposta =
                propostaRepository
                        .findById(idProposta)
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

        // O pedido é o bloqueio comum para aceitar ou enviar propostas.
        // Refresh descarta inclusive o estado carregado antes pela autorização.
        entityManager.refresh(solicitacao, LockModeType.PESSIMISTIC_WRITE);
        entityManager.refresh(proposta, LockModeType.PESSIMISTIC_WRITE);

        if (!"ENVIADA".equals(proposta.getStatus())) {
            throw new IllegalArgumentException("Esta proposta não pode ser aceita");
        }
        if (servicoRepository.existsByIdProposta(idProposta)) {
            throw new IllegalArgumentException("Já existe um serviço para esta proposta");
        }
        if (!List.of("ABERTA", "EM_NEGOCIACAO").contains(solicitacao.getStatus())) {
            throw new IllegalArgumentException("Esta solicitação não aceita mais propostas");
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

        Servico salvo = servicoRepository.save(servico);
        entityManager.flush();
        entityManager.refresh(salvo);
        return salvo;
    }

    @PreAuthorize("@acesso.servico(#idServico)")
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
    @PreAuthorize("@acesso.executarServico(#idServico)")
    public Servico iniciarServico(
            Long idServico
    ) {

        Servico servico =
                buscarParaAlterar(idServico);

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
    @PreAuthorize("@acesso.executarServico(#idServico)")
    public Servico concluirServico(
            Long idServico
    ) {

        Servico servico =
                buscarParaAlterar(idServico);

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

    @PreAuthorize("@acesso.cliente(#idCliente)")
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

    @PreAuthorize("@acesso.tecnico(#idTecnico)")
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
