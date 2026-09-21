package br.com.techhelp.service;

import br.com.techhelp.dto.CadastrarClienteRequest;
import br.com.techhelp.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CadastroServiceTest {
    final UsuarioRepository usuarios=mock(UsuarioRepository.class);
    final PasswordEncoder encoder=mock(PasswordEncoder.class);
    final CadastroService service=new CadastroService(usuarios,mock(ClienteRepository.class),
            mock(TecnicoRepository.class),mock(PerfilRepository.class),mock(UsuarioPerfilRepository.class),encoder);
    @Test void senhaMultibyteNaoPodeExcederLimiteBCrypt() {
        var request=new CadastrarClienteRequest("Teste","teste@example.test","á".repeat(37),null,"12345678901");
        assertThrows(IllegalArgumentException.class,()->service.cadastrarCliente(request));
        verifyNoInteractions(encoder);
        verify(usuarios,never()).save(any());
    }
    @Test void emailExistenteNaoCriaConta() {
        when(usuarios.existsByEmail("teste@example.test")).thenReturn(true);
        var request=new CadastrarClienteRequest("Teste","TESTE@example.test","Senha123",null,"12345678901");
        assertThrows(IllegalArgumentException.class,()->service.cadastrarCliente(request));
        verify(usuarios,never()).save(any());
        verifyNoInteractions(encoder);
    }
}
