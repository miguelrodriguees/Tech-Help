package br.com.techhelp.service;

import br.com.techhelp.model.Categoria;
import br.com.techhelp.repository.CategoriaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listarAtivas() {
        return categoriaRepository.findByAtivoTrueOrderByNomeAsc();
    }
}