package br.com.techhelp.controller;

import br.com.techhelp.dto.CriarDenunciaRequest;
import br.com.techhelp.model.Denuncia;
import br.com.techhelp.service.DenunciaService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/denuncias")
public class DenunciaController {

    private final DenunciaService denunciaService;

    public DenunciaController(
            DenunciaService denunciaService
    ) {
        this.denunciaService = denunciaService;
    }

    @PostMapping
    public ResponseEntity<Denuncia> criar(
            @Valid
            @RequestBody
            CriarDenunciaRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        denunciaService.criar(request)
                );
    }

    @GetMapping("/{idDenuncia}")
    public Denuncia buscarPorId(
            @PathVariable
            Long idDenuncia
    ) {

        return denunciaService
                .buscarPorId(idDenuncia);
    }

    @GetMapping("/status/{status}")
    public List<Denuncia> listarPorStatus(
            @PathVariable
            String status
    ) {

        return denunciaService
                .listarPorStatus(status);
    }

    @GetMapping("/denunciante/{idUsuario}")
    public List<Denuncia> listarPorDenunciante(
            @PathVariable
            Long idUsuario
    ) {

        return denunciaService
                .listarPorDenunciante(idUsuario);
    }

    @GetMapping("/denunciado/{idUsuario}")
    public List<Denuncia> listarPorDenunciado(
            @PathVariable
            Long idUsuario
    ) {

        return denunciaService
                .listarPorDenunciado(idUsuario);
    }

    @PostMapping("/{idDenuncia}/analisar")
    public Denuncia analisar(
            @PathVariable
            Long idDenuncia
    ) {

        return denunciaService
                .analisar(idDenuncia);
    }

    @PostMapping("/{idDenuncia}/resolver")
    public Denuncia resolver(
            @PathVariable
            Long idDenuncia
    ) {

        return denunciaService
                .resolver(idDenuncia);
    }

    @PostMapping("/{idDenuncia}/arquivar")
    public Denuncia arquivar(
            @PathVariable
            Long idDenuncia
    ) {

        return denunciaService
                .arquivar(idDenuncia);
    }
}