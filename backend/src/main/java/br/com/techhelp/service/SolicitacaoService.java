package br.com.techhelp.service;

import br.com.techhelp.dto.CriarSolicitacaoRequest;
import br.com.techhelp.model.Categoria;
import br.com.techhelp.model.Solicitacao;
import br.com.techhelp.repository.CategoriaRepository;
import br.com.techhelp.repository.ClienteRepository;
import br.com.techhelp.repository.EnderecoRepository;
import br.com.techhelp.repository.SolicitacaoRepository;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ClienteRepository clienteRepository;
    private final EnderecoRepository enderecoRepository;

    public SolicitacaoService(
            SolicitacaoRepository solicitacaoRepository,
            CategoriaRepository categoriaRepository,
            ClienteRepository clienteRepository,
            EnderecoRepository enderecoRepository
    ) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.categoriaRepository = categoriaRepository;
        this.clienteRepository = clienteRepository;
        this.enderecoRepository = enderecoRepository;
    }

    public Solicitacao criar(CriarSolicitacaoRequest dados, Long idCliente) {

        if (!clienteRepository.existsById(idCliente)) {
            throw new NoSuchElementException(
                    "Cliente não encontrado"
            );
        }

        Categoria categoria = categoriaRepository
                .findById(dados.idCategoria())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Categoria não encontrada"
                        )
                );

        if (!categoria.isAtivo()) {
            throw new IllegalArgumentException(
                    "Esta categoria está desativada"
            );
        }

        if (dados.idEndereco() != null) {

            var endereco = enderecoRepository
                    .findById(dados.idEndereco())
                    .orElseThrow(() ->
                            new NoSuchElementException(
                                    "Endereço não encontrado"
                            )
                    );

            var cliente = clienteRepository
                    .findById(idCliente)
                    .orElseThrow(() ->
                            new NoSuchElementException(
                                    "Cliente não encontrado"
                            )
                    );

            if (!endereco.getIdUsuario()
                    .equals(cliente.getIdUsuario())) {

                throw new IllegalArgumentException(
                        "O endereço não pertence ao cliente"
                );
            }
        }

        if (dados.orcamentoMin() != null
                && dados.orcamentoMax() != null
                && dados.orcamentoMax()
                        .compareTo(dados.orcamentoMin()) < 0) {

            throw new IllegalArgumentException(
                    "O orçamento máximo não pode ser menor que o mínimo"
            );
        }

        String tipo =
                dados.tipoAtendimento().toUpperCase();

        if (!List.of(
                "PRESENCIAL",
                "REMOTO",
                "HIBRIDO"
        ).contains(tipo)) {

            throw new IllegalArgumentException(
                    "Tipo de atendimento inválido"
            );
        }

        String urgencia =
                dados.urgencia() == null
                || dados.urgencia().isBlank()
                ? "NORMAL"
                : dados.urgencia().toUpperCase();

        if (!List.of(
                "BAIXA",
                "NORMAL",
                "ALTA",
                "EMERGENCIA"
        ).contains(urgencia)) {

            throw new IllegalArgumentException(
                    "Urgência inválida"
            );
        }

        Solicitacao solicitacao =
                new Solicitacao();

        solicitacao.setIdCliente(
                idCliente
        );

        solicitacao.setIdCategoria(
                dados.idCategoria()
        );

        solicitacao.setIdEndereco(
                dados.idEndereco()
        );

        solicitacao.setTitulo(
                dados.titulo()
        );

        solicitacao.setDescricao(
                dados.descricao()
        );

        solicitacao.setTipoAtendimento(
                tipo
        );

        solicitacao.setUrgencia(
                urgencia
        );

        solicitacao.setOrcamentoMin(
                dados.orcamentoMin()
        );

        solicitacao.setOrcamentoMax(
                dados.orcamentoMax()
        );

        solicitacao.setStatus(
                "ABERTA"
        );

        return solicitacaoRepository
                .save(solicitacao);
    }

    public List<Solicitacao> listarAbertas() {

        return solicitacaoRepository
                .findByStatusOrderByDataCadastroDesc(
                        "ABERTA"
                );
    }

    public List<Solicitacao> listarPorCliente(
            Long idCliente
    ) {

        if (!clienteRepository.existsById(idCliente)) {
            throw new NoSuchElementException(
                    "Cliente não encontrado"
            );
        }

        return solicitacaoRepository
                .findByIdClienteOrderByDataCadastroDesc(
                        idCliente
                );
    }

    public Solicitacao buscarPorId(Long id) {

        return solicitacaoRepository
                .findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Solicitação não encontrada"
                        )
                );
    }
}