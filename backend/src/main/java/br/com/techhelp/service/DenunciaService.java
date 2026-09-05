package br.com.techhelp.service;

import br.com.techhelp.dto.CriarDenunciaRequest;
import br.com.techhelp.model.Denuncia;
import br.com.techhelp.repository.DenunciaRepository;
import br.com.techhelp.repository.ServicoRepository;
import br.com.techhelp.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.time.LocalDateTime;

@Service
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;

    public DenunciaService(
            DenunciaRepository denunciaRepository,
            UsuarioRepository usuarioRepository,
            ServicoRepository servicoRepository
    ) {
        this.denunciaRepository = denunciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicoRepository = servicoRepository;
    }

    public Denuncia criar(
            CriarDenunciaRequest request
    ) {

        if (!usuarioRepository.existsById(
                request.idDenunciante()
        )) {
            throw new NoSuchElementException(
                    "Usuário denunciante não encontrado"
            );
        }

        if (!usuarioRepository.existsById(
                request.idUsuarioDenunciado()
        )) {
            throw new NoSuchElementException(
                    "Usuário denunciado não encontrado"
            );
        }

        if (request.idDenunciante().equals(
                request.idUsuarioDenunciado()
        )) {
            throw new IllegalArgumentException(
                    "Um usuário não pode denunciar a si mesmo"
            );
        }

        if (request.idServico() != null
                && !servicoRepository.existsById(
                        request.idServico()
                )) {
            throw new NoSuchElementException(
                    "Serviço não encontrado"
            );
        }

        Denuncia denuncia = new Denuncia();

        denuncia.setIdDenunciante(
                request.idDenunciante()
        );

        denuncia.setIdUsuarioDenunciado(
                request.idUsuarioDenunciado()
        );

        denuncia.setIdServico(
                request.idServico()
        );

        denuncia.setMotivo(
                request.motivo()
        );

        denuncia.setDescricao(
                request.descricao()
        );

        denuncia.setStatus("ABERTA");

        return denunciaRepository.save(
                denuncia
        );
    }

    public Denuncia buscarPorId(
            Long idDenuncia
    ) {

        return denunciaRepository
                .findById(idDenuncia)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Denúncia não encontrada"
                        ));
    }

    public List<Denuncia> listarPorStatus(
            String status
    ) {

        return denunciaRepository
                .findByStatusOrderByDataCadastroDesc(
                        status
                );
    }

    public List<Denuncia> listarPorDenunciante(
            Long idUsuario
    ) {

        return denunciaRepository
                .findByIdDenuncianteOrderByDataCadastroDesc(
                        idUsuario
                );
    }

    public List<Denuncia> listarPorDenunciado(
            Long idUsuario
    ) {

        return denunciaRepository
                .findByIdUsuarioDenunciadoOrderByDataCadastroDesc(
                        idUsuario
                );
    }

    @Transactional
    public Denuncia analisar(
            Long idDenuncia
    ) {

        Denuncia denuncia =
                buscarPorId(idDenuncia);

        if (!"ABERTA".equals(
                denuncia.getStatus()
        )) {
            throw new IllegalArgumentException(
                    "Somente denúncias abertas podem entrar em análise"
            );
        }

        denuncia.setStatus("EM_ANALISE");

        return denunciaRepository.save(
                denuncia
        );
    }

    @Transactional
    public Denuncia resolver(
            Long idDenuncia
    ) {

        Denuncia denuncia =
                buscarPorId(idDenuncia);

        if (!"EM_ANALISE".equals(
                denuncia.getStatus()
        )) {
            throw new IllegalArgumentException(
                    "A denúncia precisa estar em análise"
            );
        }

        denuncia.setStatus("RESOLVIDA");
        denuncia.setDataResolucao(LocalDateTime.now());

        return denunciaRepository.save(denuncia);
    }

    @Transactional
    public Denuncia arquivar(
            Long idDenuncia
    ) {

        Denuncia denuncia =
                buscarPorId(idDenuncia);

        if ("RESOLVIDA".equals(denuncia.getStatus())
                || "ARQUIVADA".equals(denuncia.getStatus())) {

            throw new IllegalArgumentException(
                    "Esta denúncia não pode ser arquivada"
            );
        }

        denuncia.setStatus("ARQUIVADA");

        return denunciaRepository.save(
                denuncia
        );
    }
}