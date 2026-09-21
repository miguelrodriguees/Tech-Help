package br.com.techhelp.service;

import br.com.techhelp.model.Usuario;
import br.com.techhelp.model.Cliente;
import br.com.techhelp.repository.UsuarioRepository;
import br.com.techhelp.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClienteAutenticadoTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final ClienteAutenticado service = new ClienteAutenticado(usuarios, clientes);

    @Test void usaOClienteDaIdentidadeAutenticada() {
        var usuario = new Usuario(); usuario.setIdUsuario(7L); usuario.setStatus("ATIVO");
        var cliente = new Cliente(); cliente.setIdCliente(31L); cliente.setIdUsuario(7L);
        when(usuarios.findByEmail("cliente@example.test")).thenReturn(Optional.of(usuario));
        when(clientes.findByIdUsuario(7L)).thenReturn(Optional.of(cliente));
        assertEquals(31L, service.id(() -> "cliente@example.test"));
        verify(clientes).findByIdUsuario(7L);
    }
    @Test void rejeitaSessaoAusente() {
        assertEquals(401, assertThrows(ResponseStatusException.class, () -> service.id(null)).getStatusCode().value());
        verifyNoInteractions(usuarios, clientes);
    }
    @Test void rejeitaContaInativaMesmoComSessaoExistente() {
        var usuario = new Usuario(); usuario.setStatus("INATIVO");
        when(usuarios.findByEmail("inativo@example.test")).thenReturn(Optional.of(usuario));
        assertEquals(401, assertThrows(ResponseStatusException.class, () -> service.id(() -> "inativo@example.test")).getStatusCode().value());
        verifyNoInteractions(clientes);
    }
    @Test void rejeitaUsuarioSemPerfilDeCliente() {
        var usuario = new Usuario(); usuario.setIdUsuario(9L);
        when(usuarios.findByEmail("tecnico@example.test")).thenReturn(Optional.of(usuario));
        when(clientes.findByIdUsuario(9L)).thenReturn(Optional.empty());
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> service.id(() -> "tecnico@example.test")).getStatusCode().value());
    }
}
