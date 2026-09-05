package br.com.techhelp.controller;

import br.com.techhelp.model.Cliente;
import br.com.techhelp.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                clienteService.buscarPorId(id)
        );
    }
}