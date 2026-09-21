package br.com.techhelp.service;

import br.com.techhelp.model.Servico;
import br.com.techhelp.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AtendimentoServiceTest {
    final ServicoRepository repo=mock(ServicoRepository.class);
    final ServicoService fluxo=mock(ServicoService.class);
    final AvaliacaoService avaliacoes=mock(AvaliacaoService.class);
    final AtendimentoService service=new AtendimentoService(repo,fluxo,avaliacoes,mock(ClienteRepository.class),mock(TecnicoRepository.class));
    Servico preparar(String status) {
        var s=new Servico();s.setIdCliente(2L);s.setIdTecnico(3L);s.setStatus(status);
        when(repo.buscarParaAtualizar(1L)).thenReturn(Optional.of(s));return s;
    }
    @Test void outroTecnicoNaoPodeIniciar() {
        preparar("AGENDADO");
        assertThrows(ResponseStatusException.class,()->service.iniciar(1L,99L));
        verifyNoInteractions(fluxo);
    }
    @Test void outroClienteNaoPodeConcluir() {
        preparar("EM_ANDAMENTO");
        assertThrows(ResponseStatusException.class,()->service.concluir(1L,99L));
        verifyNoInteractions(fluxo);
    }
    @Test void repetirInicioNaoAlteraData() {
        var s=preparar("EM_ANDAMENTO");
        assertSame(s,service.iniciar(1L,3L));verifyNoInteractions(fluxo);
    }
    @Test void repetirConclusaoNaoAlteraData() {
        var s=preparar("CONCLUIDO");
        assertSame(s,service.concluir(1L,2L));verifyNoInteractions(fluxo);
    }
    @Test void terceiroNaoAvalia() {
        preparar("CONCLUIDO");
        assertThrows(ResponseStatusException.class,()->service.avaliar(1L,99L,5,"Bom"));
        verifyNoInteractions(avaliacoes);
    }
    @Test void participantesCorretosAcionamFluxo() {
        preparar("AGENDADO");service.iniciar(1L,3L);verify(fluxo).iniciarServico(1L);
        preparar("EM_ANDAMENTO");service.concluir(1L,2L);verify(fluxo).concluirServico(1L);
    }
}
