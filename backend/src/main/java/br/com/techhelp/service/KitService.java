package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.dto.CriarKitRequest;
import br.com.techhelp.model.Kit;
import br.com.techhelp.repository.KitRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@PreAuthorize("@acesso.admin()")
public class KitService {

    private final KitRepository kitRepository;

    public KitService(
            KitRepository kitRepository
    ) {
        this.kitRepository = kitRepository;
    }

    public Kit criar(
            CriarKitRequest request
    ) {

        if (kitRepository
                .existsByNomeIgnoreCase(request.nome())) {

            throw new IllegalArgumentException(
                    "Já existe um kit com esse nome"
            );
        }

        Kit kit = new Kit();

        kit.setNome(request.nome());
        kit.setDescricao(request.descricao());
        kit.setValorDiaria(request.valorDiaria());
        kit.setStatus("DISPONIVEL");

        return kitRepository.save(kit);
    }

    @PreAuthorize("permitAll()")
    public List<Kit> listar() {
        return kitRepository.findAll();
    }

    @PreAuthorize("permitAll()")
    public List<Kit> listarDisponiveis() {

        return kitRepository
                .findByStatusOrderByNomeAsc(
                        "DISPONIVEL"
                );
    }

    @PreAuthorize("permitAll()")
    public Kit buscarPorId(Long idKit) {

        return kitRepository
                .findById(idKit)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Kit não encontrado"
                        ));
    }
}
