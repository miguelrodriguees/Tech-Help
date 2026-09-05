package br.com.techhelp.service;

import br.com.techhelp.dto.VincularEspecialidadeRequest;
import br.com.techhelp.model.Especialidade;
import br.com.techhelp.model.TecnicoEspecialidade;
import br.com.techhelp.repository.EspecialidadeRepository;
import br.com.techhelp.repository.TecnicoEspecialidadeRepository;
import br.com.techhelp.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TecnicoEspecialidadeService {

    private final TecnicoEspecialidadeRepository tecnicoEspecialidadeRepository;
    private final TecnicoRepository tecnicoRepository;
    private final EspecialidadeRepository especialidadeRepository;

    public TecnicoEspecialidadeService(
            TecnicoEspecialidadeRepository tecnicoEspecialidadeRepository,
            TecnicoRepository tecnicoRepository,
            EspecialidadeRepository especialidadeRepository
    ) {
        this.tecnicoEspecialidadeRepository = tecnicoEspecialidadeRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.especialidadeRepository = especialidadeRepository;
    }

    public TecnicoEspecialidade vincular(
            VincularEspecialidadeRequest request
    ) {

        if (!tecnicoRepository.existsById(request.idTecnico())) {
            throw new NoSuchElementException("Técnico não encontrado");
        }

        Especialidade especialidade = especialidadeRepository
                .findById(request.idEspecialidade())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Especialidade não encontrada"
                        ));

        if (!especialidade.isAtivo()) {
            throw new IllegalArgumentException(
                    "Esta especialidade está inativa"
            );
        }

        if (tecnicoEspecialidadeRepository
                .existsByIdTecnicoAndIdEspecialidade(
                        request.idTecnico(),
                        request.idEspecialidade()
                )) {

            throw new IllegalArgumentException(
                    "O técnico já possui esta especialidade"
            );
        }

        TecnicoEspecialidade vinculo =
                new TecnicoEspecialidade();

        vinculo.setIdTecnico(request.idTecnico());
        vinculo.setIdEspecialidade(request.idEspecialidade());

        return tecnicoEspecialidadeRepository.save(vinculo);
    }

    public List<Especialidade> listarEspecialidadesDoTecnico(
            Long idTecnico
    ) {

        if (!tecnicoRepository.existsById(idTecnico)) {
            throw new NoSuchElementException("Técnico não encontrado");
        }

        return tecnicoEspecialidadeRepository
                .findByIdTecnico(idTecnico)
                .stream()
                .map(vinculo ->
                        especialidadeRepository
                                .findById(vinculo.getIdEspecialidade())
                                .orElseThrow()
                )
                .toList();
    }
}