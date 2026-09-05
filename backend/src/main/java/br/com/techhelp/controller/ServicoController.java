package br.com.techhelp.controller;

import br.com.techhelp.model.Servico;
import br.com.techhelp.service.ServicoService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(
            ServicoService servicoService
    ) {
        this.servicoService = servicoService;
    }

    @PostMapping("/aceitar-proposta/{idProposta}")
    public ResponseEntity<Servico> aceitarProposta(
            @PathVariable Long idProposta
    ) {

        Servico servico =
                servicoService
                        .aceitarProposta(idProposta);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servico);
    }

    @GetMapping("/{idServico}")
    public Servico buscarPorId(
            @PathVariable Long idServico
    ) {

        return servicoService
                .buscarPorId(idServico);
    }

    @PostMapping("/{idServico}/iniciar")
    public Servico iniciar(
            @PathVariable Long idServico
    ) {

        return servicoService
                .iniciarServico(idServico);
    }

    @PostMapping("/{idServico}/concluir")
    public Servico concluir(
            @PathVariable Long idServico
    ) {

        return servicoService
                .concluirServico(idServico);
    }

    @GetMapping("/cliente/{idCliente}")
    public List<Servico> listarPorCliente(
            @PathVariable Long idCliente
    ) {

        return servicoService
                .listarPorCliente(idCliente);
    }

    @GetMapping("/tecnico/{idTecnico}")
    public List<Servico> listarPorTecnico(
            @PathVariable Long idTecnico
    ) {

        return servicoService
                .listarPorTecnico(idTecnico);
    }
}