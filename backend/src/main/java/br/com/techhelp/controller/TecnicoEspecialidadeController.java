package br.com.techhelp.controller;

import br.com.techhelp.dto.VincularEspecialidadeRequest;
import br.com.techhelp.model.Especialidade;
import br.com.techhelp.model.TecnicoEspecialidade;
import br.com.techhelp.service.TecnicoEspecialidadeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tecnicos")
public class TecnicoEspecialidadeController {

    private final TecnicoEspecialidadeService tecnicoEspecialidadeService;

    public TecnicoEspecialidadeController(
            TecnicoEspecialidadeService tecnicoEspecialidadeService
    ) {
        this.tecnicoEspecialidadeService =
                tecnicoEspecialidadeService;
    }

    @PostMapping("/especialidades")
    public ResponseEntity<TecnicoEspecialidade> vincular(
            @Valid @RequestBody VincularEspecialidadeRequest request
    ) {

        TecnicoEspecialidade vinculo =
                tecnicoEspecialidadeService.vincular(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vinculo);
    }

    @GetMapping("/{idTecnico}/especialidades")
    public ResponseEntity<List<Especialidade>> listarEspecialidades(
            @PathVariable Long idTecnico
    ) {
        return ResponseEntity.ok(
                tecnicoEspecialidadeService
                        .listarEspecialidadesDoTecnico(idTecnico)
        );
    }
}