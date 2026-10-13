package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.dto.CriarAluguelRequest;
import br.com.techhelp.dto.AlugueisAdminResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import br.com.techhelp.model.Aluguel;
import br.com.techhelp.model.AluguelItem;
import br.com.techhelp.model.Kit;

import br.com.techhelp.repository.AluguelItemRepository;
import br.com.techhelp.repository.AluguelRepository;
import br.com.techhelp.repository.KitRepository;
import br.com.techhelp.repository.UsuarioRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@PreAuthorize("@acesso.admin()")
public class AluguelService {

    private final AluguelRepository aluguelRepository;
    private final AluguelItemRepository aluguelItemRepository;
    private final UsuarioRepository usuarioRepository;
    private final KitRepository kitRepository;
    private final EntityManager entityManager;

    public AluguelService(
            AluguelRepository aluguelRepository,
            AluguelItemRepository aluguelItemRepository,
            UsuarioRepository usuarioRepository,
            KitRepository kitRepository,
            EntityManager entityManager
    ) {
        this.aluguelRepository = aluguelRepository;
        this.aluguelItemRepository = aluguelItemRepository;
        this.usuarioRepository = usuarioRepository;
        this.kitRepository = kitRepository;
        this.entityManager = entityManager;
    }

    @PreAuthorize("@acesso.mesmoUsuario(#request.idUsuario())")
    public Aluguel criar(CriarAluguelRequest request) {

        if (!usuarioRepository.existsById(request.idUsuario())) {

            throw new NoSuchElementException(
                    "Usuário não encontrado"
            );
        }

        if (!request.dataPrevistaDevolucao()
                .isAfter(request.dataPrevistaRetirada())) {

            throw new IllegalArgumentException(
                    "A devolução precisa ser depois da retirada"
            );
        }

        Aluguel aluguel = new Aluguel();

        aluguel.setIdUsuario(
                request.idUsuario()
        );

        aluguel.setDataPrevistaRetirada(
                request.dataPrevistaRetirada()
        );

        aluguel.setDataPrevistaDevolucao(
                request.dataPrevistaDevolucao()
        );

        aluguel.setStatus("RESERVADO");

        return aluguelRepository.save(aluguel);
    }

    @PreAuthorize("@acesso.aluguel(#idAluguel)")
    public Aluguel buscarPorId(Long idAluguel) {

        return aluguelRepository
                .findById(idAluguel)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Aluguel não encontrado"
                        ));
    }

    @PreAuthorize("@acesso.usuario(#idUsuario)")
    public List<Aluguel> listarPorUsuario(
            Long idUsuario
    ) {

        if (!usuarioRepository.existsById(idUsuario)) {

            throw new NoSuchElementException(
                    "Usuário não encontrado"
            );
        }

        return aluguelRepository
                .findByIdUsuarioOrderByDataCadastroDesc(
                        idUsuario
                );
    }

    @Transactional(readOnly = true)
    public AlugueisAdminResponse listarParaAdministracao(int pagina) {
        if (pagina < 0 || pagina > 100000) {
            throw new IllegalArgumentException("Página inválida");
        }
        var resultado = aluguelRepository.findAll(PageRequest.of(pagina, 20,
                Sort.by(Sort.Direction.DESC, "idAluguel")));
        var ids = resultado.getContent().stream().map(Aluguel::getIdUsuario).distinct().toList();
        var nomes = usuarioRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(u -> u.getIdUsuario(), u -> u.getNome()));
        var itens = resultado.getContent().stream().map(a ->
                new AlugueisAdminResponse.Item(a, nomes.getOrDefault(a.getIdUsuario(), "Usuário #" + a.getIdUsuario()))).toList();
        return new AlugueisAdminResponse(itens, resultado.getNumber(), resultado.getTotalPages());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Aluguel retirar(Long idAluguel) {

        Aluguel aluguel =
                buscarPorId(idAluguel);
        entityManager.refresh(aluguel, LockModeType.PESSIMISTIC_WRITE);

        if (!"RESERVADO".equals(
                aluguel.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Somente aluguéis reservados podem ser retirados"
            );
        }

        if (aluguelItemRepository
                .countByIdAluguel(idAluguel) == 0) {

            throw new IllegalArgumentException(
                    "O aluguel precisa possuir pelo menos um kit"
            );
        }

        aluguel.setStatus("RETIRADO");

        aluguel.setDataRetirada(
                LocalDateTime.now()
        );

        return aluguelRepository.save(aluguel);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Aluguel devolver(Long idAluguel) {

        Aluguel aluguel =
                buscarPorId(idAluguel);
        entityManager.refresh(aluguel, LockModeType.PESSIMISTIC_WRITE);

        if (!"RETIRADO".equals(aluguel.getStatus())
                && !"ATRASADO".equals(aluguel.getStatus())) {

            throw new IllegalArgumentException(
                    "Este aluguel não pode ser devolvido"
            );
        }

        aluguel.setStatus("DEVOLVIDO");

        aluguel.setDataDevolucao(
                LocalDateTime.now()
        );

        liberarKits(idAluguel);

        return aluguelRepository.save(aluguel);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("@acesso.aluguel(#idAluguel)")
    public Aluguel cancelar(Long idAluguel) {

        Aluguel aluguel =
                buscarPorId(idAluguel);
        entityManager.refresh(aluguel, LockModeType.PESSIMISTIC_WRITE);

        if (!"RESERVADO".equals(
                aluguel.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Somente aluguéis reservados podem ser cancelados"
            );
        }

        aluguel.setStatus("CANCELADO");

        liberarKits(idAluguel);

        return aluguelRepository.save(aluguel);
    }

    private void liberarKits(Long idAluguel) {

        List<AluguelItem> itens =
                aluguelItemRepository
                        .findByIdAluguelOrderByIdAluguelItemAsc(
                                idAluguel
                        );

        itens = itens.stream().sorted(Comparator.comparing(AluguelItem::getIdKit)).toList();
        for (AluguelItem item : itens) {

            Kit kit =
                    kitRepository
                            .findById(item.getIdKit())
                            .orElseThrow(() ->
                                    new NoSuchElementException(
                                            "Kit não encontrado"
                                    ));

            entityManager.refresh(kit, LockModeType.PESSIMISTIC_WRITE);
            if ("INDISPONIVEL".equals(kit.getStatus())
                    && aluguelItemRepository.contarReservasAtivasDoKit(kit.getIdKit()) == 0) {
                kit.setStatus("DISPONIVEL");
            }

            kitRepository.save(kit);
        }
    }
}
