package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.model.Tecnico;
import br.com.techhelp.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@PreAuthorize("@acesso.admin()")
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;

    public TecnicoService(TecnicoRepository tecnicoRepository) {
        this.tecnicoRepository = tecnicoRepository;
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
