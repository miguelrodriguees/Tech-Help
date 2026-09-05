package br.com.techhelp.service;

import br.com.techhelp.model.Tecnico;
import br.com.techhelp.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;

    public TecnicoService(TecnicoRepository tecnicoRepository) {
        this.tecnicoRepository = tecnicoRepository;
    }

    public Tecnico buscarPorId(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Técnico não encontrado"));
    }

    public List<Tecnico> listarTodos() {
        return tecnicoRepository.findAll();
    }
}