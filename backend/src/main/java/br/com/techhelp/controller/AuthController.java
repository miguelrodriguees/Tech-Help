package br.com.techhelp.controller;

import br.com.techhelp.dto.SessaoResponse;
import br.com.techhelp.service.AcessoService;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AcessoService acesso;

    public AuthController(AcessoService acesso) {
        this.acesso = acesso;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @GetMapping("/me")
    public SessaoResponse me() {
        return acesso.sessao();
    }
}
