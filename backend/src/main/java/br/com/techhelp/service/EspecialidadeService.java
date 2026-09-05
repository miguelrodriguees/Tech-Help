package br.com.techhelp.service;

import br.com.techhelp.model.Especialidade;
import br.com.techhelp.repository.EspecialidadeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EspecialidadeService {

    private final EspecialidadeRepository especialidadeRepository;

    public EspecialidadeService(
            EspecialidadeRepository especialidadeRepository
    ) {
        this.especialidadeRepository = especialidadeRepository;
    }

    public List<Especialidade> listarAtivas() {
        return especialidadeRepository.findByAtivoTrueOrderByNomeAsc();
    }

    public List<Especialidade> listarPorCategoria(Long idCategoria) {
        return especialidadeRepository
                .findByIdCategoriaAndAtivoTrueOrderByNomeAsc(idCategoria);
    }
}