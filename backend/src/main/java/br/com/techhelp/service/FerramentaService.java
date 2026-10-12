package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.dto.CriarFerramentaRequest;
import br.com.techhelp.dto.AtualizarFerramentaRequest;
import br.com.techhelp.repository.AluguelItemRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import br.com.techhelp.model.Ferramenta;
import br.com.techhelp.repository.FerramentaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@PreAuthorize("@acesso.admin()")
public class FerramentaService {

    private final FerramentaRepository ferramentaRepository;
    private final AluguelItemRepository aluguelItens;
    private final EntityManager entityManager;

    public FerramentaService(
            FerramentaRepository ferramentaRepository,
            AluguelItemRepository aluguelItens,
            EntityManager entityManager
    ) {
        this.ferramentaRepository = ferramentaRepository;
        this.aluguelItens = aluguelItens;
        this.entityManager = entityManager;
    }

    public Ferramenta criar(
            CriarFerramentaRequest request
    ) {

        if (ferramentaRepository
                .existsByNomeIgnoreCase(request.nome())) {

            throw new IllegalArgumentException(
                    "Já existe uma ferramenta com esse nome"
            );
        }

        Ferramenta ferramenta = new Ferramenta();

        ferramenta.setNome(request.nome());
        ferramenta.setDescricao(request.descricao());
        ferramenta.setQuantidadeTotal(
                request.quantidadeTotal()
        );

        ferramenta.setQuantidadeDisponivel(
                request.quantidadeTotal()
        );

        ferramenta.setValorDiaria(
                request.valorDiaria()
        );

        ferramenta.setStatus("DISPONIVEL");

        return ferramentaRepository.save(ferramenta);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Ferramenta atualizar(Long id, AtualizarFerramentaRequest request) {
        var ferramenta = buscarPorId(id);
        entityManager.refresh(ferramenta, LockModeType.PESSIMISTIC_WRITE);
        if (aluguelItens.quantidadeComprometida(id) > 0) {
            throw new IllegalArgumentException("Ferramenta comprometida por aluguel ativo; aguarde cancelamento ou devolução");
        }
        String nome = request.nome().trim();
        if (ferramentaRepository.existsByNomeIgnoreCaseAndIdFerramentaNot(nome, id)) {
            throw new IllegalArgumentException("Já existe uma ferramenta com esse nome");
        }
        // Preserva unidades já indisponíveis por outros motivos, sem confundi-las com reservas.
        long indisponiveis = Math.max(0L, (long) ferramenta.getQuantidadeTotal() - ferramenta.getQuantidadeDisponivel());
        if (request.quantidadeTotal() < indisponiveis) {
            throw new IllegalArgumentException("O total não pode ser menor que as unidades já indisponíveis");
        }
        ferramenta.setNome(nome); ferramenta.setDescricao(request.descricao());
        ferramenta.setQuantidadeTotal(request.quantidadeTotal());
        ferramenta.setQuantidadeDisponivel((int) (request.quantidadeTotal() - indisponiveis));
        ferramenta.setValorDiaria(request.valorDiaria()); ferramenta.setStatus(request.status());
        return ferramentaRepository.save(ferramenta);
    }

    @PreAuthorize("permitAll()")
    public List<Ferramenta> listar() {
        return ferramentaRepository.findAll();
    }

    @PreAuthorize("permitAll()")
    public List<Ferramenta> listarDisponiveis() {

        return ferramentaRepository
                .findByStatusAndQuantidadeDisponivelGreaterThanOrderByNomeAsc(
                        "DISPONIVEL",
                        0
                );
    }

    @PreAuthorize("permitAll()")
    public Ferramenta buscarPorId(
            Long idFerramenta
    ) {

        return ferramentaRepository
                .findById(idFerramenta)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Ferramenta não encontrada"
                        ));
    }
}
