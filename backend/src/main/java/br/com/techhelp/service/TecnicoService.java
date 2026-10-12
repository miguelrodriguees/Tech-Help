package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.model.Tecnico;
import br.com.techhelp.dto.TecnicoResumoResponse;
import br.com.techhelp.model.Usuario;
import br.com.techhelp.repository.UsuarioRepository;
import br.com.techhelp.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@PreAuthorize("@acesso.admin()")
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;
    private final UsuarioRepository usuarioRepository;

    public TecnicoService(TecnicoRepository tecnicoRepository, UsuarioRepository usuarioRepository) {
        this.tecnicoRepository = tecnicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PreAuthorize("permitAll()")
    public List<TecnicoResumoResponse> resumir(List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > 50
                || ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Informe de 1 a 50 identificadores de técnicos válidos");
        }
        var tecnicos = tecnicoRepository.findAllById(ids.stream().distinct().toList());
        var usuarios = usuarioRepository.findAllById(tecnicos.stream()
                .map(Tecnico::getIdUsuario).distinct().toList()).stream()
                .filter(u -> "ATIVO".equals(u.getStatus()))
                .filter(u -> u.getNome() != null && !u.getNome().isBlank())
                .collect(Collectors.toMap(Usuario::getIdUsuario, Usuario::getNome));
        return tecnicos.stream().filter(t -> usuarios.containsKey(t.getIdUsuario()))
                .map(t -> new TecnicoResumoResponse(t.getIdTecnico(), usuarios.get(t.getIdUsuario())))
                .toList();
    }

    @PreAuthorize("permitAll()")
    public Tecnico buscarPorId(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Técnico não encontrado"));
    }

    @PreAuthorize("permitAll()")
    public List<Tecnico> listarTodos() {
        return tecnicoRepository.findAll();
    }
}
