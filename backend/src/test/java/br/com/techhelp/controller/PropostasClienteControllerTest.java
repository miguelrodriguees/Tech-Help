package br.com.techhelp.controller;

import br.com.techhelp.model.Solicitacao;
import br.com.techhelp.repository.*;
import br.com.techhelp.service.ClienteAutenticado;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PropostasClienteControllerTest {
    final ClienteAutenticado cliente=mock(ClienteAutenticado.class);
    final SolicitacaoRepository pedidos=mock(SolicitacaoRepository.class);
    final PropostaRepository propostas=mock(PropostaRepository.class);
    final PropostasClienteController controller=new PropostasClienteController(cliente,pedidos,propostas,mock(TecnicoRepository.class),mock(UsuarioRepository.class));
    final Principal principal=()->"cliente@example.test";
    @Test void terceiroNaoConsultaPropostas() {
        var pedido=new Solicitacao();pedido.setIdCliente(2L);
        when(cliente.id(principal)).thenReturn(99L);when(pedidos.findById(1L)).thenReturn(Optional.of(pedido));
        assertEquals(404,assertThrows(ResponseStatusException.class,()->controller.listar(1L,principal)).getStatusCode().value());
        verifyNoInteractions(propostas);
    }
    @Test void donoPodeConsultarPedidoSemPropostas() {
        var pedido=new Solicitacao();pedido.setIdCliente(2L);
        when(cliente.id(principal)).thenReturn(2L);when(pedidos.findById(1L)).thenReturn(Optional.of(pedido));
        when(propostas.findByIdSolicitacaoOrderByDataCadastroDesc(1L)).thenReturn(List.of());
        assertTrue(controller.listar(1L,principal).isEmpty());
    }
}
