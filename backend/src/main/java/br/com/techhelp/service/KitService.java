package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.dto.CriarKitRequest;
import br.com.techhelp.dto.AtualizarKitRequest;
import br.com.techhelp.repository.AluguelItemRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import br.com.techhelp.model.Kit;
import br.com.techhelp.repository.KitRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@PreAuthorize("@acesso.admin()")
public class KitService {

    private final KitRepository kitRepository;
    private final EstoqueAluguelService estoque;
    private final AluguelItemRepository aluguelItens;
    private final EntityManager entityManager;

    public KitService(
            KitRepository kitRepository,
            EstoqueAluguelService estoque,
            AluguelItemRepository aluguelItens,
            EntityManager entityManager
    ) {
        this.kitRepository = kitRepository;
        this.estoque = estoque;
        this.aluguelItens = aluguelItens;
        this.entityManager = entityManager;
    }

    public Kit criar(
            CriarKitRequest request
    ) {

        if (kitRepository
                .existsByNomeIgnoreCase(request.nome())) {

            throw new IllegalArgumentException(
                    "Já existe um kit com esse nome"
            );
        }

        Kit kit = new Kit();

        kit.setNome(request.nome());
        kit.setDescricao(request.descricao());
        kit.setValorDiaria(request.valorDiaria());
        kit.setStatus("DISPONIVEL");

        return kitRepository.save(kit);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Kit atualizar(Long id, AtualizarKitRequest request) {
        var kit = buscarPorId(id);
        entityManager.refresh(kit, LockModeType.PESSIMISTIC_WRITE);
        if (aluguelItens.contarReservasAtivasDoKit(id) > 0) {
            throw new IllegalArgumentException("Kit com aluguel ativo; aguarde cancelamento ou devolução");
        }
        String nome = request.nome().trim();
        if (kitRepository.existsByNomeIgnoreCaseAndIdKitNot(nome, id)) {
            throw new IllegalArgumentException("Já existe um kit com esse nome");
        }
        kit.setNome(nome); kit.setDescricao(request.descricao());
        kit.setValorDiaria(request.valorDiaria()); kit.setStatus(request.status());
        return kitRepository.save(kit);
    }

    @PreAuthorize("permitAll()")
    public List<Kit> listar() {
        return kitRepository.findAll();
    }

    @PreAuthorize("permitAll()")
    public List<Kit> listarDisponiveis() {

        return kitRepository
                .findByStatusOrderByNomeAsc(
                        "DISPONIVEL"
                ).stream().filter(kit -> estoque.quantidadeDisponivel(kit) > 0).toList();
    }

    @PreAuthorize("permitAll()")
    public Kit buscarPorId(Long idKit) {

        return kitRepository
                .findById(idKit)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Kit não encontrado"
                        ));
    }
}
