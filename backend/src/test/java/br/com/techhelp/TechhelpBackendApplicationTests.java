package br.com.techhelp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import br.com.techhelp.repository.AluguelItemRepository;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TechhelpBackendApplicationTests {

    @Autowired AluguelItemRepository aluguelItens;

    @Test
    void consultasDeEstoqueExecutamNoBancoSemAlterarRegistros() {
        assertThat(aluguelItens.quantidadeComprometida(Long.MAX_VALUE)).isGreaterThanOrEqualTo(0);
        assertThat(aluguelItens.contarReservasAtivasDoKit(Long.MAX_VALUE)).isGreaterThanOrEqualTo(0);
    }

	@Test
	void contextLoads() {
	}

}
