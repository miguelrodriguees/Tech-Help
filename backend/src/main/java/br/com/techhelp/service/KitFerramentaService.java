package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.dto.AdicionarFerramentaKitRequest;
import br.com.techhelp.model.Ferramenta;
import br.com.techhelp.model.KitFerramenta;
import br.com.techhelp.model.KitFerramentaId;
import br.com.techhelp.repository.FerramentaRepository;
import br.com.techhelp.repository.KitFerramentaRepository;
import br.com.techhelp.repository.KitRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@PreAuthorize("@acesso.admin()")
public class KitFerramentaService {

    private final KitFerramentaRepository kitFerramentaRepository;
    private final KitRepository kitRepository;
    private final FerramentaRepository ferramentaRepository;

    public KitFerramentaService(
            KitFerramentaRepository kitFerramentaRepository,
            KitRepository kitRepository,
            FerramentaRepository ferramentaRepository
    ) {
        this.kitFerramentaRepository = kitFerramentaRepository;
        this.kitRepository = kitRepository;
        this.ferramentaRepository = ferramentaRepository;
    }

    public KitFerramenta adicionar(
            Long idKit,
            AdicionarFerramentaKitRequest request
    ) {

        if (!kitRepository.existsById(idKit)) {
            throw new NoSuchElementException(
                    "Kit não encontrado"
            );
        }

        Ferramenta ferramenta =
                ferramentaRepository
                        .findById(request.idFerramenta())
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Ferramenta não encontrada"
                                ));

        if (kitFerramentaRepository
                .existsByIdKitAndIdFerramenta(
                        idKit,
                        request.idFerramenta()
                )) {

            throw new IllegalArgumentException(
                    "Esta ferramenta já pertence ao kit"
            );
        }

        if (request.quantidade()
                > ferramenta.getQuantidadeTotal()) {

            throw new IllegalArgumentException(
                    "Quantidade maior que o estoque total da ferramenta"
            );
        }

        KitFerramenta item = new KitFerramenta();

        item.setIdKit(idKit);
        item.setIdFerramenta(
                request.idFerramenta()
        );

        item.setQuantidade(
                request.quantidade()
        );

        return kitFerramentaRepository.save(item);
    }

    @PreAuthorize("permitAll()")
    public List<KitFerramenta> listar(
            Long idKit
    ) {

        if (!kitRepository.existsById(idKit)) {
            throw new NoSuchElementException(
                    "Kit não encontrado"
            );
        }

        return kitFerramentaRepository
                .findByIdKit(idKit);
    }

    public void remover(
            Long idKit,
            Long idFerramenta
    ) {

        KitFerramentaId id =
                new KitFerramentaId(
                        idKit,
                        idFerramenta
                );

        if (!kitFerramentaRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "Ferramenta não encontrada neste kit"
            );
        }

        kitFerramentaRepository.deleteById(id);
    }
}
