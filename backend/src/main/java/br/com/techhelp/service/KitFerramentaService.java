package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.dto.AdicionarFerramentaKitRequest;
import br.com.techhelp.model.Ferramenta;
import br.com.techhelp.model.KitFerramenta;
import br.com.techhelp.model.KitFerramentaId;
import br.com.techhelp.repository.FerramentaRepository;
import br.com.techhelp.repository.AluguelItemRepository;
import br.com.techhelp.repository.KitFerramentaRepository;
import br.com.techhelp.repository.KitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@PreAuthorize("@acesso.admin()")
public class KitFerramentaService {

    private final KitFerramentaRepository kitFerramentaRepository;
    private final KitRepository kitRepository;
    private final EntityManager entityManager;
    private final AluguelItemRepository aluguelItens;
    private final FerramentaRepository ferramentaRepository;

    public KitFerramentaService(
            KitFerramentaRepository kitFerramentaRepository,
            KitRepository kitRepository,
            FerramentaRepository ferramentaRepository,
            EntityManager entityManager,
            AluguelItemRepository aluguelItens
    ) {
        this.kitFerramentaRepository = kitFerramentaRepository;
        this.kitRepository = kitRepository;
        this.entityManager = entityManager;
        this.aluguelItens = aluguelItens;
        this.ferramentaRepository = ferramentaRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public KitFerramenta adicionar(
            Long idKit,
            AdicionarFerramentaKitRequest request
    ) {

        if (!kitRepository.existsById(idKit)) {
            throw new NoSuchElementException(
                    "Kit não encontrado"
            );
        }

        bloquearKitLivre(idKit);

        Ferramenta ferramenta =
                ferramentaRepository
                        .findById(request.idFerramenta())
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Ferramenta não encontrada"
                                ));

        if (kitFerramentaRepository
                .existsByIdKitAndIdFerramenta(
                        idKit,
                        request.idFerramenta()
                )) {

            throw new IllegalArgumentException(
                    "Esta ferramenta já pertence ao kit"
            );
        }

        if (request.quantidade()
                > ferramenta.getQuantidadeTotal()) {

            throw new IllegalArgumentException(
                    "Quantidade maior que o estoque total da ferramenta"
            );
        }

        KitFerramenta item = new KitFerramenta();

        item.setIdKit(idKit);
        item.setIdFerramenta(
                request.idFerramenta()
        );

        item.setQuantidade(
                request.quantidade()
        );

        return kitFerramentaRepository.save(item);
    }

    @PreAuthorize("permitAll()")
    public List<KitFerramenta> listar(
            Long idKit
    ) {

        if (!kitRepository.existsById(idKit)) {
            throw new NoSuchElementException(
                    "Kit não encontrado"
            );
        }

        return kitFerramentaRepository
                .findByIdKit(idKit);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void remover(
            Long idKit,
            Long idFerramenta
    ) {

        bloquearKitLivre(idKit);

        KitFerramentaId id =
                new KitFerramentaId(
                        idKit,
                        idFerramenta
                );

        if (!kitFerramentaRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "Ferramenta não encontrada neste kit"
            );
        }

        kitFerramentaRepository.deleteById(id);
    }

    private void bloquearKitLivre(Long idKit) {
        var kit = kitRepository.findById(idKit)
                .orElseThrow(() -> new NoSuchElementException("Kit não encontrado"));
        entityManager.refresh(kit, LockModeType.PESSIMISTIC_WRITE);
        if (aluguelItens.contarReservasAtivasDoKit(idKit) > 0) {
            throw new IllegalArgumentException("Não é possível alterar a composição de um kit com aluguel ativo");
        }
    }
}
