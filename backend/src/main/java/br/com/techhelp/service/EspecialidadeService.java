package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.model.Especialidade;
import br.com.techhelp.repository.EspecialidadeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@PreAuthorize("@acesso.admin()")
public class EspecialidadeService {

    private final EspecialidadeRepository especialidadeRepository;

    public EspecialidadeService(
            EspecialidadeRepository especialidadeRepository
    ) {
        this.especialidadeRepository = especialidadeRepository;
    }

    @PreAuthorize("permitAll()")
    public List<Especialidade> listarAtivas() {
        return especialidadeRepository.findByAtivoTrueOrderByNomeAsc();
    }

    @PreAuthorize("permitAll()")
    public List<Especialidade> listarPorCategoria(Long idCategoria) {
        return especialidadeRepository
                .findByIdCategoriaAndAtivoTrueOrderByNomeAsc(idCategoria);
    }
}
