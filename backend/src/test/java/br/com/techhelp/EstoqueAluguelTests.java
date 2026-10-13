package br.com.techhelp;

import br.com.techhelp.dto.AdicionarItemAluguelRequest;
import br.com.techhelp.dto.AdicionarFerramentaKitRequest;
import br.com.techhelp.model.*;
import br.com.techhelp.repository.*;
import br.com.techhelp.service.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class EstoqueAluguelTests {
    AluguelItemRepository itens = mock(AluguelItemRepository.class);
    AluguelRepository alugueis = mock(AluguelRepository.class);
    KitRepository kits = mock(KitRepository.class);
    KitFerramentaRepository composicoes = mock(KitFerramentaRepository.class);
    FerramentaRepository ferramentas = mock(FerramentaRepository.class);
    EntityManager em = mock(EntityManager.class);
    EstoqueAluguelService estoque = new EstoqueAluguelService(composicoes, ferramentas, itens, em);
    AluguelItemService itemService = new AluguelItemService(itens, alugueis, kits, em, estoque);
    AluguelService aluguelService = new AluguelService(alugueis, itens, mock(UsuarioRepository.class), kits, em);
    KitFerramentaService composicaoService = new KitFerramentaService(composicoes, kits, ferramentas, em, itens);
    Kit kit;
    Ferramenta ferramenta;
    Aluguel aluguel;
    List<AluguelItem> salvos;

    @BeforeEach
    void preparar() {
        kit = new Kit(); kit.setIdKit(1L); kit.setValorDiaria(new BigDecimal("15.00"));
        ferramenta = new Ferramenta(); ferramenta.setIdFerramenta(10L);
        ferramenta.setQuantidadeTotal(10); ferramenta.setQuantidadeDisponivel(10);
        KitFerramenta componente = new KitFerramenta(); componente.setIdKit(1L);
        componente.setIdFerramenta(10L); componente.setQuantidade(2);
        aluguel = new Aluguel(); aluguel.setIdAluguel(100L); aluguel.setIdUsuario(1L);
        aluguel.setDataPrevistaRetirada(LocalDateTime.of(2026, 11, 1, 10, 0));
        aluguel.setDataPrevistaDevolucao(LocalDateTime.of(2026, 11, 3, 10, 0));
        salvos = new ArrayList<>();
        when(kits.findById(1L)).thenReturn(Optional.of(kit));
        when(kits.existsById(1L)).thenReturn(true);
        when(ferramentas.findById(10L)).thenReturn(Optional.of(ferramenta));
        when(composicoes.findByIdKit(1L)).thenReturn(List.of(componente));
        when(alugueis.findById(100L)).thenReturn(Optional.of(aluguel));
        when(alugueis.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(itens.findByIdAluguelOrderByIdAluguelItemAsc(100L)).thenAnswer(inv -> new ArrayList<>(salvos));
        when(itens.save(any())).thenAnswer(inv -> {
            AluguelItem item = inv.getArgument(0); item.setIdAluguelItem(200L); salvos.add(item); return item;
        });
        when(itens.findById(200L)).thenAnswer(inv -> salvos.stream().findFirst());
        doAnswer(inv -> { salvos.remove(inv.getArgument(0)); return null; }).when(itens).delete(any());
    }

    @Test void multiplicaComponentesPelaQuantidadeECalculaDiarias() {
        itemService.adicionar(100L, new AdicionarItemAluguelRequest(1L, 3));
        assertThat(salvos).hasSize(1);
        assertThat(aluguel.getValorTotal()).isEqualByComparingTo("90.00");
        assertThat(kit.getStatus()).isEqualTo("INDISPONIVEL");
        assertThat(ferramenta.getQuantidadeDisponivel()).isEqualTo(10);
    }

    @Test void rejeitaQuantidadeAcimaDoEstoqueSemGravarItem() {
        assertThatThrownBy(() -> itemService.adicionar(100L, new AdicionarItemAluguelRequest(1L, 6)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(itens, never()).save(any());
        assertThat(kit.getStatus()).isEqualTo("DISPONIVEL");
    }

    @Test void descontaFerramentasComprometidasPorOutrosKits() {
        when(itens.quantidadeComprometida(10L)).thenReturn(7L);
        assertThat(estoque.quantidadeDisponivel(kit)).isEqualTo(1);
        assertThatThrownBy(() -> itemService.adicionar(100L, new AdicionarItemAluguelRequest(1L, 2)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(itens, never()).save(any());
    }

    @Test void respeitaLimiteDisponivelCadastrado() {
        ferramenta.setQuantidadeDisponivel(4);
        when(itens.quantidadeComprometida(10L)).thenReturn(2L);
        assertThat(estoque.quantidadeDisponivel(kit)).isEqualTo(1);
    }

    @Test void estoqueHistoricoExcedidoNaoGeraDisponibilidadeNegativa() {
        when(itens.quantidadeComprometida(10L)).thenReturn(20L);
        assertThat(estoque.quantidadeDisponivel(kit)).isZero();
    }

    @Test void kitVazioOuFerramentaEmManutencaoNaoPodeSerReservado() {
        ferramenta.setStatus("MANUTENCAO");
        assertThatThrownBy(() -> estoque.validarReserva(kit, 1)).isInstanceOf(IllegalArgumentException.class);
        ferramenta.setStatus("DISPONIVEL");
        when(composicoes.findByIdKit(1L)).thenReturn(List.of());
        assertThatThrownBy(() -> estoque.validarReserva(kit, 1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void usaComponenteMaisEscasso() {
        Ferramenta outra = new Ferramenta(); outra.setIdFerramenta(11L);
        outra.setQuantidadeTotal(3); outra.setQuantidadeDisponivel(3);
        KitFerramenta c = new KitFerramenta(); c.setIdKit(1L); c.setIdFerramenta(11L); c.setQuantidade(2);
        List<KitFerramenta> lista = new ArrayList<>(composicoes.findByIdKit(1L)); lista.add(c);
        when(composicoes.findByIdKit(1L)).thenReturn(lista);
        when(ferramentas.findById(11L)).thenReturn(Optional.of(outra));
        assertThat(estoque.quantidadeDisponivel(kit)).isEqualTo(1);
    }

    @Test void reservaHistoricaBloqueiaKitMesmoComStatusIncorreto() {
        when(itens.contarReservasAtivasDoKit(1L)).thenReturn(1L);
        assertThatThrownBy(() -> estoque.validarReserva(kit, 1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void catalogoDisponivelExcluiKitSemEstoque() {
        when(kits.findByStatusOrderByNomeAsc("DISPONIVEL")).thenReturn(List.of(kit));
        when(itens.quantidadeComprometida(10L)).thenReturn(10L);
        assertThat(new KitService(kits, estoque, itens, em).listarDisponiveis()).isEmpty();
    }

    @Test void removerItemRecalculaTotalELiberaKit() {
        itemService.adicionar(100L, new AdicionarItemAluguelRequest(1L, 2));
        itemService.remover(100L, 200L);
        assertThat(salvos).isEmpty();
        assertThat(aluguel.getValorTotal()).isEqualByComparingTo("0");
        assertThat(kit.getStatus()).isEqualTo("DISPONIVEL");
    }

    @Test void cancelarNaoReativaKitInativoNemAlteraEstoqueCadastrado() {
        itemService.adicionar(100L, new AdicionarItemAluguelRequest(1L, 2));
        kit.setStatus("INATIVO");
        aluguelService.cancelar(100L);
        assertThat(kit.getStatus()).isEqualTo("INATIVO");
        assertThat(aluguel.getStatus()).isEqualTo("CANCELADO");
        assertThat(ferramenta.getQuantidadeDisponivel()).isEqualTo(10);
        assertThatThrownBy(() -> aluguelService.cancelar(100L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void devolverLiberaKitENaoAceitaSegundaDevolucao() {
        itemService.adicionar(100L, new AdicionarItemAluguelRequest(1L, 2));
        aluguel.setStatus("RETIRADO");
        aluguelService.devolver(100L);
        assertThat(kit.getStatus()).isEqualTo("DISPONIVEL");
        assertThat(aluguel.getStatus()).isEqualTo("DEVOLVIDO");
        assertThatThrownBy(() -> aluguelService.devolver(100L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void cancelarNaoLiberaKitAindaComprometidoEmOutroAluguel() {
        itemService.adicionar(100L, new AdicionarItemAluguelRequest(1L, 2));
        when(itens.contarReservasAtivasDoKit(1L)).thenReturn(1L);
        aluguelService.cancelar(100L);
        assertThat(kit.getStatus()).isEqualTo("INDISPONIVEL");
    }

    @Test void naoPermiteAlterarComposicaoComAluguelAtivo() {
        when(itens.contarReservasAtivasDoKit(1L)).thenReturn(1L);
        assertThatThrownBy(() -> composicaoService.adicionar(1L, new AdicionarFerramentaKitRequest(10L, 1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> composicaoService.remover(1L, 10L))
                .isInstanceOf(IllegalArgumentException.class);
        verify(composicoes, never()).save(any());
        verify(composicoes, never()).deleteById(any());
    }

    @Test void naoModificaItensDepoisDaRetirada() {
        aluguel.setStatus("RETIRADO");
        assertThatThrownBy(() -> itemService.adicionar(100L, new AdicionarItemAluguelRequest(1L, 1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> itemService.remover(100L, 200L)).isInstanceOf(IllegalArgumentException.class);
        verify(itens, never()).save(any());
        verify(itens, never()).delete(any());
    }
}
