package br.com.techhelp.service;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class LocacaoServiceTest {
    final JdbcTemplate db=mock(JdbcTemplate.class);
    final LocacaoService service=new LocacaoService(db);
    Map<String,Object> rental(String status){return Map.of("id_aluguel",10L,"id_usuario",1L,"chave_pedido","teste","status",status);}
    @Test void outroUsuarioNaoPodeAceitar(){
        when(db.queryForList("SELECT * FROM aluguel WHERE id_aluguel=? FOR UPDATE",10L)).thenReturn(List.of(rental("AGUARDANDO_ACEITE")));
        var error=assertThrows(ResponseStatusException.class,()->service.accept(2L,10L));assertEquals(404,error.getStatusCode().value());
        verify(db,never()).update(anyString(),any(Object[].class));
    }
    @Test void outroUsuarioNaoPodeCancelar(){
        when(db.queryForList("SELECT * FROM aluguel WHERE id_aluguel=? FOR UPDATE",10L)).thenReturn(List.of(rental("RESERVADO")));
        assertThrows(ResponseStatusException.class,()->service.cancel(2L,10L,false));
        verify(db,never()).update(anyString(),any(Object[].class));
    }
    @Test void naoAceitaTaxaAindaNaoCotada(){
        when(db.queryForList("SELECT * FROM aluguel WHERE id_aluguel=? FOR UPDATE",10L)).thenReturn(List.of(rental("AGUARDANDO_TAXA")));
        assertThrows(IllegalArgumentException.class,()->service.accept(1L,10L));
    }
    @Test void naoEntregaAntesDoAceite(){
        when(db.queryForList("SELECT * FROM aluguel WHERE id_aluguel=? FOR UPDATE",10L)).thenReturn(List.of(rental("AGUARDANDO_ACEITE")));
        assertThrows(IllegalArgumentException.class,()->service.handover(10L));
    }
    @Test void naoDevolveAntesDaRetirada(){
        when(db.queryForList("SELECT * FROM aluguel WHERE id_aluguel=? FOR UPDATE",10L)).thenReturn(List.of(rental("RESERVADO")));
        assertThrows(IllegalArgumentException.class,()->service.returned(10L));
    }
}
