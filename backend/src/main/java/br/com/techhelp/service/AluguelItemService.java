package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.dto.AdicionarItemAluguelRequest;
import br.com.techhelp.model.Aluguel;
import br.com.techhelp.model.AluguelItem;
import br.com.techhelp.model.Kit;
import br.com.techhelp.repository.AluguelItemRepository;
import br.com.techhelp.repository.AluguelRepository;
import br.com.techhelp.repository.KitRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@PreAuthorize("@acesso.admin()")
public class AluguelItemService {

    private final AluguelItemRepository aluguelItemRepository;
    private final AluguelRepository aluguelRepository;
    private final KitRepository kitRepository;
    private final EntityManager entityManager;
    private final EstoqueAluguelService estoque;

    public AluguelItemService(
            AluguelItemRepository aluguelItemRepository,
            AluguelRepository aluguelRepository,
            KitRepository kitRepository,
            EntityManager entityManager,
            EstoqueAluguelService estoque
    ) {
        this.aluguelItemRepository = aluguelItemRepository;
        this.aluguelRepository = aluguelRepository;
        this.kitRepository = kitRepository;
        this.entityManager = entityManager;
        this.estoque = estoque;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("@acesso.aluguel(#idAluguel)")
    public AluguelItem adicionar(
            Long idAluguel,
            AdicionarItemAluguelRequest request
    ) {

        Aluguel aluguel =
                aluguelRepository
                        .findById(idAluguel)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Aluguel não encontrado"
                                ));

        entityManager.refresh(aluguel, LockModeType.PESSIMISTIC_WRITE);

        if (!"RESERVADO".equals(aluguel.getStatus())) {
            throw new IllegalArgumentException(
                    "Só é possível adicionar itens em um aluguel reservado"
            );
        }

        Kit kit =
                kitRepository
                        .findById(request.idKit())
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Kit não encontrado"
                                ));

        entityManager.refresh(kit, LockModeType.PESSIMISTIC_WRITE);

        if (!"DISPONIVEL".equals(kit.getStatus())) {
            throw new IllegalArgumentException(
                    "Este kit não está disponível"
            );
        }

        if (aluguelItemRepository
                .existsByIdAluguelAndIdKit(
                        idAluguel,
                        request.idKit()
                )) {

            throw new IllegalArgumentException(
                    "Este kit já foi adicionado ao aluguel"
            );
        }

        estoque.validarReserva(kit, request.quantidade());

        AluguelItem item = new AluguelItem();

        item.setIdAluguel(idAluguel);
        item.setIdKit(request.idKit());
        item.setQuantidade(request.quantidade());

        item.setValorDiaria(
                kit.getValorDiaria()
        );

        AluguelItem salvo =
                aluguelItemRepository.save(item);

        kit.setStatus("INDISPONIVEL");
        kitRepository.save(kit);

        recalcularValorTotal(aluguel);

        return salvo;
    }

    @PreAuthorize("@acesso.aluguel(#idAluguel)")
    public List<AluguelItem> listar(
            Long idAluguel
    ) {

        if (!aluguelRepository.existsById(idAluguel)) {
            throw new NoSuchElementException(
                    "Aluguel não encontrado"
            );
        }

        return aluguelItemRepository
                .findByIdAluguelOrderByIdAluguelItemAsc(
                        idAluguel
                );
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("@acesso.aluguel(#idAluguel)")
    public void remover(
            Long idAluguel,
            Long idAluguelItem
    ) {

        Aluguel aluguel =
                aluguelRepository
                        .findById(idAluguel)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Aluguel não encontrado"
                                ));

        entityManager.refresh(aluguel, LockModeType.PESSIMISTIC_WRITE);

        if (!"RESERVADO".equals(aluguel.getStatus())) {
            throw new IllegalArgumentException(
                    "Não é possível remover itens deste aluguel"
            );
        }

        AluguelItem item =
                aluguelItemRepository
                        .findById(idAluguelItem)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Item não encontrado"
                                ));

        if (!item.getIdAluguel().equals(idAluguel)) {
            throw new IllegalArgumentException(
                    "O item não pertence a este aluguel"
            );
        }

        Kit kit =
                kitRepository
                        .findById(item.getIdKit())
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Kit não encontrado"
                                ));

        entityManager.refresh(kit, LockModeType.PESSIMISTIC_WRITE);
        aluguelItemRepository.delete(item);

        if ("INDISPONIVEL".equals(kit.getStatus())
                && aluguelItemRepository.contarReservasAtivasDoKit(kit.getIdKit()) == 0) {
            kit.setStatus("DISPONIVEL");
        }
        kitRepository.save(kit);

        recalcularValorTotal(aluguel);
    }

    private void recalcularValorTotal(
            Aluguel aluguel
    ) {

        List<AluguelItem> itens =
                aluguelItemRepository
                        .findByIdAluguelOrderByIdAluguelItemAsc(
                                aluguel.getIdAluguel()
                        );

        long dias =
                ChronoUnit.DAYS.between(
                        aluguel.getDataPrevistaRetirada()
                                .toLocalDate(),
                        aluguel.getDataPrevistaDevolucao()
                                .toLocalDate()
                );

        if (dias < 1) {
            dias = 1;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (AluguelItem item : itens) {

            BigDecimal subtotal =
                    item.getValorDiaria()
                            .multiply(
                                    BigDecimal.valueOf(
                                            item.getQuantidade()
                                    )
                            )
                            .multiply(
                                    BigDecimal.valueOf(dias)
                            );

            total = total.add(subtotal);
        }

        aluguel.setValorTotal(total);

        aluguelRepository.save(aluguel);
    }
}
