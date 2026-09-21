package br.com.techhelp.service;

import br.com.techhelp.model.*;
import br.com.techhelp.repository.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AceitarPropostaTest {
    final ServicoRepository servicos=mock(ServicoRepository.class);
    final PropostaRepository propostas=mock(PropostaRepository.class);
    final SolicitacaoRepository pedidos=mock(SolicitacaoRepository.class);
    final ServicoService service=new ServicoService(servicos,propostas,pedidos,mock(ClienteRepository.class),mock(TecnicoRepository.class));
    final Proposta proposta=new Proposta();
    final Solicitacao pedido=new Solicitacao();
    @BeforeEach void preparar() {
        proposta.setIdProposta(1L);proposta.setIdSolicitacao(2L);proposta.setIdTecnico(3L);proposta.setStatus("ENVIADA");proposta.setValor(new BigDecimal("120.00"));
        pedido.setIdSolicitacao(2L);pedido.setIdCliente(4L);pedido.setStatus("EM_NEGOCIACAO");
        when(propostas.findById(1L)).thenReturn(Optional.of(proposta));
        when(pedidos.buscarParaProposta(2L)).thenReturn(Optional.of(pedido));
        when(servicos.findByIdProposta(1L)).thenReturn(Optional.empty());
        when(propostas.findByIdSolicitacaoAndStatus(2L,"ENVIADA")).thenReturn(List.of());
        when(servicos.save(any(Servico.class))).thenAnswer(c -> c.getArgument(0));
    }
    @Test void somenteDonoPodeAceitar() {
        assertThrows(ResponseStatusException.class,()->service.aceitarProposta(1L,99L));
        verify(servicos,never()).save(any());verify(propostas,never()).save(any());
    }
    @Test void contrataERecusaOutras() {
        var outra=new Proposta();outra.setIdProposta(5L);outra.setStatus("ENVIADA");
        when(propostas.findByIdSolicitacaoAndStatus(2L,"ENVIADA")).thenReturn(List.of(outra));
        var atendimento=service.aceitarProposta(1L,4L);
        assertEquals("ACEITA",proposta.getStatus());assertEquals("RECUSADA",outra.getStatus());
        assertEquals("CONTRATADA",pedido.getStatus());assertEquals(4L,atendimento.getIdCliente());
        assertEquals(proposta.getValor(),atendimento.getValorAcordado());
        assertNull(atendimento.getDataAgendada());
    }
    @Test void repeticaoRetornaMesmoAtendimento() {
        var existente=new Servico();pedido.setStatus("CONTRATADA");
        when(servicos.findByIdProposta(1L)).thenReturn(Optional.of(existente));
        assertSame(existente,service.aceitarProposta(1L,4L));
        verify(servicos,never()).save(any());
    }
    @Test void impedeSegundaContratacao() {
        pedido.setStatus("CONTRATADA");
        assertThrows(IllegalArgumentException.class,()->service.aceitarProposta(1L,4L));
        verify(servicos,never()).save(any());
    }
}
