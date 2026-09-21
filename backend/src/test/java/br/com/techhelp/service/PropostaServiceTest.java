package br.com.techhelp.service;

import br.com.techhelp.dto.CriarPropostaRequest;
import br.com.techhelp.model.*;
import br.com.techhelp.repository.*;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PropostaServiceTest {
    private final PropostaRepository propostas = mock(PropostaRepository.class);
    private final SolicitacaoRepository pedidos = mock(SolicitacaoRepository.class);
    private final TecnicoRepository tecnicos = mock(TecnicoRepository.class);
    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final PropostaService service = new PropostaService(propostas, pedidos, tecnicos, clientes);
    private final Solicitacao pedido = new Solicitacao();
    private final Tecnico tecnico = new Tecnico();
    private final CriarPropostaRequest dados = new CriarPropostaRequest(1L, new BigDecimal("150.00"), "Diagnóstico", (short) 2, null);

    @BeforeEach void preparar() {
        pedido.setIdCliente(3L); pedido.setStatus("ABERTA");
        tecnico.setIdUsuario(10L);
        var cliente = new Cliente(); cliente.setIdUsuario(20L);
        when(pedidos.buscarParaProposta(1L)).thenReturn(Optional.of(pedido));
        when(tecnicos.existsById(7L)).thenReturn(true);
        when(tecnicos.findById(7L)).thenReturn(Optional.of(tecnico));
        when(clientes.findById(3L)).thenReturn(Optional.of(cliente));
        when(propostas.save(any(Proposta.class))).thenAnswer(call -> call.getArgument(0));
    }
    @Test void usaTecnicoAutenticadoEAbreNegociacao() {
        var criada = service.criar(dados, 7L);
        assertEquals(7L, criada.getIdTecnico());
        assertEquals("ENVIADA", criada.getStatus());
        assertEquals("EM_NEGOCIACAO", pedido.getStatus());
        verify(pedidos).save(pedido);
    }
    @Test void bloqueiaDuplicada() {
        when(propostas.existsByIdSolicitacaoAndIdTecnico(1L, 7L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.criar(dados, 7L));
        verify(propostas, never()).save(any());
    }
    @Test void bloqueiaProprioPedido() {
        tecnico.setIdUsuario(20L);
        assertThrows(IllegalArgumentException.class, () -> service.criar(dados, 7L));
        verify(propostas, never()).save(any());
    }
    @Test void bloqueiaPedidoEncerrado() {
        pedido.setStatus("CONCLUIDA");
        assertThrows(IllegalArgumentException.class, () -> service.criar(dados, 7L));
        verify(propostas, never()).save(any());
    }
}
