package br.com.techhelp.service;

import br.com.techhelp.dto.CriarPagamentoRequest;
import br.com.techhelp.model.Pagamento;
import br.com.techhelp.repository.AluguelRepository;
import br.com.techhelp.repository.PagamentoRepository;
import br.com.techhelp.repository.ServicoRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime; 

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final ServicoRepository servicoRepository;
    private final AluguelRepository aluguelRepository;

    private static final Set<String> FORMAS_PERMITIDAS =
            Set.of(
                    "PIX",
                    "CARTAO_CREDITO",
                    "CARTAO_DEBITO",
                    "BOLETO",
                    "DINHEIRO"
            );

    public PagamentoService(
            PagamentoRepository pagamentoRepository,
            ServicoRepository servicoRepository,
            AluguelRepository aluguelRepository
    ) {
        this.pagamentoRepository = pagamentoRepository;
        this.servicoRepository = servicoRepository;
        this.aluguelRepository = aluguelRepository;
    }

    public Pagamento criar(
            CriarPagamentoRequest request
    ) {

        if (request.idServico() == null
                && request.idAluguel() == null) {

            throw new IllegalArgumentException(
                    "Informe um serviço ou um aluguel"
            );
        }

        if (request.idServico() != null
                && request.idAluguel() != null) {

            throw new IllegalArgumentException(
                    "O pagamento deve pertencer a um serviço ou a um aluguel"
            );
        }

        if (!FORMAS_PERMITIDAS.contains(
                request.formaPagamento()
        )) {

            throw new IllegalArgumentException(
                    "Forma de pagamento inválida"
            );
        }

        if (request.idServico() != null
                && !servicoRepository.existsById(
                        request.idServico()
                )) {

            throw new NoSuchElementException(
                    "Serviço não encontrado"
            );
        }

        if (request.idAluguel() != null
                && !aluguelRepository.existsById(
                        request.idAluguel()
                )) {

            throw new NoSuchElementException(
                    "Aluguel não encontrado"
            );
        }

        Pagamento pagamento = new Pagamento();

        pagamento.setIdServico(
                request.idServico()
        );

        pagamento.setIdAluguel(
                request.idAluguel()
        );

        pagamento.setValor(
                request.valor()
        );

        pagamento.setFormaPagamento(
                request.formaPagamento()
        );

        pagamento.setStatus("PENDENTE");

        return pagamentoRepository.save(
                pagamento
        );
    }

    public Pagamento buscarPorId(
            Long idPagamento
    ) {

        return pagamentoRepository
                .findById(idPagamento)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Pagamento não encontrado"
                        ));
    }

    public List<Pagamento> listarPorServico(
            Long idServico
    ) {

        return pagamentoRepository
                .findByIdServicoOrderByDataCadastroDesc(
                        idServico
                );
    }

    public List<Pagamento> listarPorAluguel(
            Long idAluguel
    ) {

        return pagamentoRepository
                .findByIdAluguelOrderByDataCadastroDesc(
                        idAluguel
                );
    }

    @Transactional
    public Pagamento pagar(
            Long idPagamento
    ) {

        Pagamento pagamento =
                buscarPorId(idPagamento);

        if (!"PENDENTE".equals(
                pagamento.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Este pagamento não pode ser processado"
            );
        }

        pagamento.setStatus("PAGO");
        
        pagamento.setDataPagamento(
        LocalDateTime.now()
    );

        pagamento.setGateway(
                "SIMULADO"
        );

        pagamento.setIdTransacaoExterna(
                "SIM-" + UUID.randomUUID()
        );

        return pagamentoRepository.save(
                pagamento
        );
    }

    @Transactional
    public Pagamento cancelar(
            Long idPagamento
    ) {

        Pagamento pagamento =
                buscarPorId(idPagamento);

        if (!"PENDENTE".equals(
                pagamento.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Somente pagamentos pendentes podem ser cancelados"
            );
        }

        pagamento.setStatus(
                "CANCELADO"
        );

        return pagamentoRepository.save(
                pagamento
        );
    }
}