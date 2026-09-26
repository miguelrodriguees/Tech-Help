package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.dto.CriarFerramentaRequest;
import br.com.techhelp.model.Ferramenta;
import br.com.techhelp.repository.FerramentaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@PreAuthorize("@acesso.admin()")
public class FerramentaService {

    private final FerramentaRepository ferramentaRepository;

    public FerramentaService(
            FerramentaRepository ferramentaRepository
    ) {
        this.ferramentaRepository = ferramentaRepository;
    }

    public Ferramenta criar(
            CriarFerramentaRequest request
    ) {

        if (ferramentaRepository
                .existsByNomeIgnoreCase(request.nome())) {

            throw new IllegalArgumentException(
                    "Já existe uma ferramenta com esse nome"
            );
        }

        Ferramenta ferramenta = new Ferramenta();

        ferramenta.setNome(request.nome());
        ferramenta.setDescricao(request.descricao());
        ferramenta.setQuantidadeTotal(
                request.quantidadeTotal()
        );

        ferramenta.setQuantidadeDisponivel(
                request.quantidadeTotal()
        );

        ferramenta.setValorDiaria(
                request.valorDiaria()
        );

        ferramenta.setStatus("DISPONIVEL");

        return ferramentaRepository.save(ferramenta);
    }

    @PreAuthorize("permitAll()")
    public List<Ferramenta> listar() {
        return ferramentaRepository.findAll();
    }

    @PreAuthorize("permitAll()")
    public List<Ferramenta> listarDisponiveis() {

        return ferramentaRepository
                .findByStatusAndQuantidadeDisponivelGreaterThanOrderByNomeAsc(
                        "DISPONIVEL",
                        0
                );
    }

    @PreAuthorize("permitAll()")
    public Ferramenta buscarPorId(
            Long idFerramenta
    ) {

        return ferramentaRepository
                .findById(idFerramenta)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Ferramenta não encontrada"
                        ));
    }
}
