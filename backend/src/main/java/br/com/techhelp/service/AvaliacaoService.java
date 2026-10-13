package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.dto.CriarAvaliacaoRequest;
import br.com.techhelp.dto.AvaliarAtendimentoRequest;
import br.com.techhelp.model.Avaliacao;
import br.com.techhelp.model.Cliente;
import br.com.techhelp.model.Servico;
import br.com.techhelp.model.Tecnico;
import br.com.techhelp.repository.AvaliacaoRepository;
import br.com.techhelp.repository.ClienteRepository;
import br.com.techhelp.repository.ServicoRepository;
import br.com.techhelp.repository.TecnicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@PreAuthorize("@acesso.admin()")
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ServicoRepository servicoRepository;
    private final ClienteRepository clienteRepository;
    private final TecnicoRepository tecnicoRepository;
    private final EntityManager entityManager;

    public AvaliacaoService(
            AvaliacaoRepository avaliacaoRepository,
            ServicoRepository servicoRepository,
            ClienteRepository clienteRepository,
            TecnicoRepository tecnicoRepository,
            EntityManager entityManager
    ) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.servicoRepository = servicoRepository;
        this.clienteRepository = clienteRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.entityManager = entityManager;
    }

    @PreAuthorize("@acesso.mesmoUsuario(#request.idAvaliador()) and @acesso.servico(#request.idServico())")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Avaliacao criar(CriarAvaliacaoRequest request) {

        Servico servico = servicoRepository
                .findById(request.idServico())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Serviço não encontrado"
                        ));

        // Serializa avaliações do mesmo atendimento e relê dados carregados pela autorização.
        entityManager.refresh(servico, LockModeType.PESSIMISTIC_WRITE);
        if (!"CONCLUIDO".equals(servico.getStatus())) {
            throw new IllegalArgumentException(
                    "O serviço precisa estar concluído para ser avaliado"
            );
        }

        if (request.idAvaliador().equals(request.idAvaliado())) {
            throw new IllegalArgumentException(
                    "Um usuário não pode avaliar a si mesmo"
            );
        }

        Cliente cliente = clienteRepository
                .findById(servico.getIdCliente())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Cliente do serviço não encontrado"
                        ));

        Tecnico tecnico = tecnicoRepository
                .findById(servico.getIdTecnico())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Técnico do serviço não encontrado"
                        ));

        Long idUsuarioCliente = cliente.getIdUsuario();
        Long idUsuarioTecnico = tecnico.getIdUsuario();

        boolean clienteAvaliandoTecnico =
                request.idAvaliador().equals(idUsuarioCliente)
                && request.idAvaliado().equals(idUsuarioTecnico);

        boolean tecnicoAvaliandoCliente =
                request.idAvaliador().equals(idUsuarioTecnico)
                && request.idAvaliado().equals(idUsuarioCliente);

        if (!clienteAvaliandoTecnico
                && !tecnicoAvaliandoCliente) {

            throw new IllegalArgumentException(
                    "Somente os participantes do serviço podem se avaliar"
            );
        }

        boolean jaAvaliou =
                avaliacaoRepository
                        .existsByIdServicoAndIdAvaliadorAndIdAvaliado(
                                request.idServico(),
                                request.idAvaliador(),
                                request.idAvaliado()
                        );

        if (jaAvaliou) {
            throw new IllegalArgumentException(
                    "Esta avaliação já foi realizada"
            );
        }

        Avaliacao avaliacao = new Avaliacao();

        avaliacao.setIdServico(request.idServico());
        avaliacao.setIdAvaliador(request.idAvaliador());
        avaliacao.setIdAvaliado(request.idAvaliado());

        avaliacao.setNota(
                request.nota().byteValue()
        );

        avaliacao.setComentario(
                request.comentario()
        );

        Avaliacao salva = avaliacaoRepository.save(avaliacao);
        entityManager.flush();
        entityManager.refresh(salva);
        return salva;
    }

    @PreAuthorize("@acesso.avaliarComoCliente(#idServico)")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Avaliacao avaliarAtendimento(Long idServico, AvaliarAtendimentoRequest request) {
        Servico servico = servicoRepository.findById(idServico)
                .orElseThrow(() -> new NoSuchElementException("Serviço não encontrado"));
        Cliente cliente = clienteRepository.findById(servico.getIdCliente())
                .orElseThrow(() -> new NoSuchElementException("Cliente não encontrado"));
        Tecnico tecnico = tecnicoRepository.findById(servico.getIdTecnico())
                .orElseThrow(() -> new NoSuchElementException("Técnico não encontrado"));
        // A autorização desta entrada é feita acima; criar mantém as regras de negócio comuns.
        String comentario = request.comentario() == null ? null : request.comentario().strip();
        return criar(new CriarAvaliacaoRequest(idServico, cliente.getIdUsuario(),
                tecnico.getIdUsuario(), request.nota().intValueExact(), comentario));
    }

    @PreAuthorize("permitAll()")
    public List<Avaliacao> listarPorUsuario(
            Long idUsuario
    ) {
        return avaliacaoRepository
                .findByIdAvaliadoOrderByDataAvaliacaoDesc(
                        idUsuario
                );
    }

    @PreAuthorize("@acesso.servico(#idServico)")
    public List<Avaliacao> listarPorServico(
            Long idServico
    ) {
        return avaliacaoRepository
                .findByIdServicoOrderByDataAvaliacaoDesc(
                        idServico
                );
    }
}
