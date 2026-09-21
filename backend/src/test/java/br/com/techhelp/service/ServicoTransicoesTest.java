package br.com.techhelp.service;

import br.com.techhelp.model.*;
import br.com.techhelp.repository.*;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServicoTransicoesTest {
    final ServicoRepository servicos=mock(ServicoRepository.class);
    final PropostaRepository propostas=mock(PropostaRepository.class);
    final SolicitacaoRepository pedidos=mock(SolicitacaoRepository.class);
    final ServicoService service=new ServicoService(servicos,propostas,pedidos,mock(ClienteRepository.class),mock(TecnicoRepository.class));
    Servico preparar(String status) {
        var s=new Servico();s.setStatus(status);s.setIdProposta(2L);
        when(servicos.findById(1L)).thenReturn(Optional.of(s));
        when(servicos.save(any(Servico.class))).thenAnswer(c->c.getArgument(0));return s;
    }
    @Test void naoConcluiAntesDeIniciar() {
        preparar("AGENDADO");
        assertThrows(IllegalArgumentException.class,()->service.concluirServico(1L));
        verify(servicos,never()).save(any());verifyNoInteractions(propostas,pedidos);
    }
    @Test void naoReabreServicoEncerradoOuCancelado() {
        for(String status:new String[]{"CONCLUIDO","CANCELADO"}) {
            preparar(status);assertThrows(IllegalArgumentException.class,()->service.iniciarServico(1L));
        }
        verify(servicos,never()).save(any());
    }
    @Test void inicioRegistraDataEEstado() {
        var s=preparar("AGENDADO");service.iniciarServico(1L);
        assertEquals("EM_ANDAMENTO",s.getStatus());assertNotNull(s.getDataInicio());assertNull(s.getDataConclusao());
    }
    @Test void concluirAtualizaServicoEPedido() {
        var s=preparar("EM_ANDAMENTO");s.setDataInicio(LocalDateTime.now().minusMinutes(10));
        var p=new Proposta();p.setIdSolicitacao(3L);var pedido=new Solicitacao();pedido.setStatus("CONTRATADA");
        when(propostas.findById(2L)).thenReturn(Optional.of(p));when(pedidos.findById(3L)).thenReturn(Optional.of(pedido));
        service.concluirServico(1L);
        assertEquals("CONCLUIDO",s.getStatus());assertEquals("CONCLUIDA",pedido.getStatus());
        assertFalse(s.getDataConclusao().isBefore(s.getDataInicio()));verify(pedidos).save(pedido);
    }
}
