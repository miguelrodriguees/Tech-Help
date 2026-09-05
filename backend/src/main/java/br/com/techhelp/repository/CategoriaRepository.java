package br.com.techhelp.repository;

import br.com.techhelp.model.Categoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository
        extends JpaRepository<Categoria, Long> {

    List<Categoria> findByAtivoTrueOrderByNomeAsc();
}