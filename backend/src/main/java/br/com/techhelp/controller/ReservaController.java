package br.com.techhelp.controller;

import br.com.techhelp.dto.ReservarKitRequest;
import br.com.techhelp.model.Aluguel;
import br.com.techhelp.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alugueis")
public class ReservaController {
    private final ReservaService reservas;
    public ReservaController(ReservaService reservas) { this.reservas = reservas; }

    @PostMapping("/reservar")
    @ResponseStatus(HttpStatus.CREATED)
    public Aluguel reservar(@Valid @RequestBody ReservarKitRequest request) {
        return reservas.reservar(request);
    }
}
