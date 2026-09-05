package br.com.techhelp.service;

import br.com.techhelp.dto.CriarAvaliacaoRequest;
import br.com.techhelp.model.Avaliacao;
import br.com.techhelp.model.Cliente;
import br.com.techhelp.model.Servico;
import br.com.techhelp.model.Tecnico;
import br.com.techhelp.repository.AvaliacaoRepository;
import br.com.techhelp.repository.ClienteRepository;
import br.com.techhelp.repository.ServicoRepository;
import br.com.techhelp.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ServicoRepository servicoRepository;
    private final ClienteRepository clienteRepository;
    private final TecnicoRepository tecnicoRepository;

    public AvaliacaoService(
            AvaliacaoRepository avaliacaoRepository,
            ServicoRepository servicoRepository,
            ClienteRepository clienteRepository,
            TecnicoRepository tecnicoRepository
    ) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.servicoRepository = servicoRepository;
        this.clienteRepository = clienteRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    public Avaliacao criar(CriarAvaliacaoRequest request) {

        Servico servico = servicoRepository
                .findById(request.idServico())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Serviço não encontrado"
                        ));

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

        return avaliacaoRepository.save(avaliacao);
    }

    public List<Avaliacao> listarPorUsuario(
            Long idUsuario
    ) {
        return avaliacaoRepository
                .findByIdAvaliadoOrderByDataAvaliacaoDesc(
                        idUsuario
                );
    }

    public List<Avaliacao> listarPorServico(
            Long idServico
    ) {
        return avaliacaoRepository
                .findByIdServicoOrderByDataAvaliacaoDesc(
                        idServico
                );
    }
}