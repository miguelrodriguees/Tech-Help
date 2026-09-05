package br.com.techhelp.service;

import br.com.techhelp.dto.CriarAluguelRequest;
import br.com.techhelp.model.Aluguel;
import br.com.techhelp.model.AluguelItem;
import br.com.techhelp.model.Kit;

import br.com.techhelp.repository.AluguelItemRepository;
import br.com.techhelp.repository.AluguelRepository;
import br.com.techhelp.repository.KitRepository;
import br.com.techhelp.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AluguelService {

    private final AluguelRepository aluguelRepository;
    private final AluguelItemRepository aluguelItemRepository;
    private final UsuarioRepository usuarioRepository;
    private final KitRepository kitRepository;

    public AluguelService(
            AluguelRepository aluguelRepository,
            AluguelItemRepository aluguelItemRepository,
            UsuarioRepository usuarioRepository,
            KitRepository kitRepository
    ) {
        this.aluguelRepository = aluguelRepository;
        this.aluguelItemRepository = aluguelItemRepository;
        this.usuarioRepository = usuarioRepository;
        this.kitRepository = kitRepository;
    }

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

    public Aluguel buscarPorId(Long idAluguel) {

        return aluguelRepository
                .findById(idAluguel)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Aluguel não encontrado"
                        ));
    }

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

    @Transactional
    public Aluguel retirar(Long idAluguel) {

        Aluguel aluguel =
                buscarPorId(idAluguel);

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

    @Transactional
    public Aluguel devolver(Long idAluguel) {

        Aluguel aluguel =
                buscarPorId(idAluguel);

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

    @Transactional
    public Aluguel cancelar(Long idAluguel) {

        Aluguel aluguel =
                buscarPorId(idAluguel);

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

        for (AluguelItem item : itens) {

            Kit kit =
                    kitRepository
                            .findById(item.getIdKit())
                            .orElseThrow(() ->
                                    new NoSuchElementException(
                                            "Kit não encontrado"
                                    ));

            kit.setStatus("DISPONIVEL");

            kitRepository.save(kit);
        }
    }
}