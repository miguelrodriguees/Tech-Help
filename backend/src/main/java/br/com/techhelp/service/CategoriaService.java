package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.model.Categoria;
import br.com.techhelp.repository.CategoriaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("@acesso.admin()")
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @PreAuthorize("permitAll()")
    public List<Categoria> listarAtivas() {
        return categoriaRepository.findByAtivoTrueOrderByNomeAsc();
    }
}
