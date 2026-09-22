package br.com.techhelp.service;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class LocacaoRegrasTest {
    final LocalDateTime agora=LocalDateTime.of(2026,9,1,9,0);
    final LocalDateTime inicio=agora.plusDays(1);
    @Test void fracaoDeDiaNaoSomeNoPreco(){assertEquals(2,LocacaoRegras.diarias(inicio,inicio.plusHours(24).plusMinutes(1),agora));}
    @Test void vinteQuatroHorasContaUmaDiaria(){assertEquals(1,LocacaoRegras.diarias(inicio,inicio.plusHours(24),agora));}
    @Test void menosDeUmDiaContaUmaDiaria(){assertEquals(1,LocacaoRegras.diarias(inicio,inicio.plusMinutes(1),agora));}
    @Test void naoAceitaPassado(){assertThrows(IllegalArgumentException.class,()->LocacaoRegras.diarias(agora.minusMinutes(1),inicio,agora));}
    @Test void naoAceitaPeriodoInvertido(){assertThrows(IllegalArgumentException.class,()->LocacaoRegras.diarias(inicio,inicio,agora));}
    @Test void limitaPeriodo(){assertThrows(IllegalArgumentException.class,()->LocacaoRegras.diarias(inicio,inicio.plusDays(31),agora));}
    @Test void taxaPendenteNaoViraGratis(){assertNull(LocacaoRegras.total(new BigDecimal("30.00"),null,BigDecimal.ZERO));}
    @Test void somaIdaEVolta(){assertEquals(new BigDecimal("58.00"),LocacaoRegras.total(new BigDecimal("30.00"),new BigDecimal("15.00"),new BigDecimal("13.00")));}
}
