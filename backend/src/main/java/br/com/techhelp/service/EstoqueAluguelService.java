package br.com.techhelp.service;

import br.com.techhelp.model.Ferramenta;
import br.com.techhelp.model.Kit;
import br.com.techhelp.model.KitFerramenta;
import br.com.techhelp.repository.AluguelItemRepository;
import br.com.techhelp.repository.FerramentaRepository;
import br.com.techhelp.repository.KitFerramentaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

/** Cálculo interno: reservas ativas comprometem o estoque até a liberação. */
@Service
public class EstoqueAluguelService {
    private final KitFerramentaRepository composicoes;
    private final FerramentaRepository ferramentas;
    private final AluguelItemRepository itens;
    private final EntityManager entityManager;

    public EstoqueAluguelService(KitFerramentaRepository composicoes,
            FerramentaRepository ferramentas, AluguelItemRepository itens,
            EntityManager entityManager) {
        this.composicoes = composicoes;
        this.ferramentas = ferramentas;
        this.itens = itens;
        this.entityManager = entityManager;
    }

    // Chamado com aluguel e kit bloqueados, dentro de transação READ_COMMITTED.
    public void validarReserva(Kit kit, Integer quantidade) {
        if (quantidade == null || quantidade < 1) {
            throw new IllegalArgumentException("A quantidade precisa ser maior que zero");
        }
        if (quantidadeDisponivel(kit, true) < quantidade) {
            throw new IllegalArgumentException("Kit sem ferramentas disponíveis para a quantidade solicitada");
        }
    }

    public long quantidadeDisponivel(Kit kit) {
        return quantidadeDisponivel(kit, false);
    }

    private long quantidadeDisponivel(Kit kit, boolean bloquear) {
        if (!"DISPONIVEL".equals(kit.getStatus())
                || itens.contarReservasAtivasDoKit(kit.getIdKit()) > 0) return 0;
        List<KitFerramenta> componentes = composicoes.findByIdKit(kit.getIdKit()).stream()
                .sorted(Comparator.comparing(KitFerramenta::getIdFerramenta)).toList();
        if (componentes.isEmpty()) return 0;
        long disponivel = Long.MAX_VALUE;
        for (KitFerramenta componente : componentes) {
            Ferramenta ferramenta = ferramentas.findById(componente.getIdFerramenta())
                    .orElseThrow(() -> new NoSuchElementException("Ferramenta do kit não encontrada"));
            // Ordem por ID evita inversão dos bloqueios entre kits que compartilham ferramentas.
            if (bloquear) entityManager.refresh(ferramenta, LockModeType.PESSIMISTIC_WRITE);
            if (!"DISPONIVEL".equals(ferramenta.getStatus())
                    || componente.getQuantidade() == null || componente.getQuantidade() < 1) return 0;
            // Preserva o limite cadastrado, inclusive indisponibilidades anteriores.
            long capacidade = Math.min(ferramenta.getQuantidadeTotal(), ferramenta.getQuantidadeDisponivel());
            long livre = Math.max(0, capacidade - itens.quantidadeComprometida(ferramenta.getIdFerramenta()));
            disponivel = Math.min(disponivel, livre / componente.getQuantidade());
        }
        return disponivel;
    }
}
