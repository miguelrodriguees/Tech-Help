package br.com.techhelp.controller;

import br.com.techhelp.dto.LocacaoDtos.*;
import br.com.techhelp.service.LocacaoService;
import br.com.techhelp.service.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/locacao")
public class LocacaoController {
    private final LocacaoService service;
    private final UsuarioAutenticado session;
    public LocacaoController(LocacaoService service,UsuarioAutenticado session){this.service=service;this.session=session;}
    @GetMapping("/catalogo") public List<Map<String,Object>> catalog(){return service.catalog();}
    @GetMapping("/meus") public List<Map<String,Object>> mine(Principal p){return service.mine(session.id(p));}
    @PostMapping("/pedidos") public Map<String,Object> create(Principal p,@Valid @RequestBody Pedido data){return service.create(session.id(p),data);}
    @PostMapping("/pedidos/{id}/aceitar") public Map<String,Object> accept(Principal p,@PathVariable Long id){return service.accept(session.id(p),id);}
    @PostMapping("/pedidos/{id}/cancelar") public Map<String,Object> cancel(Principal p,@PathVariable Long id){return service.cancel(session.id(p),id,false);}
    @GetMapping("/admin/pedidos") public List<Map<String,Object>> operations(Principal p){session.exigirAdmin(p);return service.operations();}
    @GetMapping("/admin/ferramentas") public List<Map<String,Object>> inventory(Principal p){session.exigirAdmin(p);return service.inventory();}
    @PostMapping("/admin/ferramentas") @ResponseStatus(HttpStatus.CREATED)
    public void tool(Principal p,@Valid @RequestBody Ferramenta data){session.exigirAdmin(p);service.addTool(data);}
    @PostMapping("/admin/kits") @ResponseStatus(HttpStatus.CREATED)
    public void kit(Principal p,@Valid @RequestBody Kit data){session.exigirAdmin(p);service.addKit(data);}
    @PostMapping("/admin/ferramentas/{id}/status") public void status(Principal p,@PathVariable Long id,@Valid @RequestBody Estado data){session.exigirAdmin(p);service.toolStatus(id,data.status());}
    @PostMapping("/admin/pedidos/{id}/taxas") public Map<String,Object> quote(Principal p,@PathVariable Long id,@Valid @RequestBody Taxas data){session.exigirAdmin(p);return service.quote(id,data);}
    @PostMapping("/admin/pedidos/{id}/entregar") public Map<String,Object> handover(Principal p,@PathVariable Long id){session.exigirAdmin(p);return service.handover(id);}
    @PostMapping("/admin/pedidos/{id}/devolver") public Map<String,Object> returned(Principal p,@PathVariable Long id){session.exigirAdmin(p);return service.returned(id);}
    @PostMapping("/admin/pedidos/{id}/cancelar") public Map<String,Object> cancelAdmin(Principal p,@PathVariable Long id){return service.cancel(session.exigirAdmin(p),id,true);}
}
