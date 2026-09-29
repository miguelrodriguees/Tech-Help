package br.com.techhelp.service;

import org.springframework.security.access.prepost.PreAuthorize;

import br.com.techhelp.dto.CriarNotificacaoRequest;
import br.com.techhelp.model.Notificacao;
import br.com.techhelp.repository.NotificacaoRepository;
import br.com.techhelp.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@PreAuthorize("@acesso.admin()")
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Notificacao criar(
            CriarNotificacaoRequest request
    ) {

        if (!usuarioRepository.existsById(request.idUsuario())) {
            throw new NoSuchElementException(
                    "Usuário não encontrado"
            );
        }

        Notificacao notificacao = new Notificacao();

        notificacao.setIdUsuario(request.idUsuario());
        notificacao.setTipo(request.tipo());
        notificacao.setTitulo(request.titulo());
        notificacao.setMensagem(request.mensagem());
        notificacao.setLinkDestino(request.linkDestino());
        notificacao.setLida(false);

        return notificacaoRepository.save(notificacao);
    }

    @PreAuthorize("@acesso.usuario(#idUsuario)")
    public List<Notificacao> listarPorUsuario(
            Long idUsuario
    ) {

        validarUsuario(idUsuario);

        return notificacaoRepository
                .findByIdUsuarioOrderByDataCadastroDesc(
                        idUsuario
                );
    }

    @PreAuthorize("@acesso.usuario(#idUsuario)")
    public List<Notificacao> listarNaoLidas(
            Long idUsuario
    ) {

        validarUsuario(idUsuario);

        return notificacaoRepository
                .findByIdUsuarioAndLidaFalseOrderByDataCadastroDesc(
                        idUsuario
                );
    }

    @PreAuthorize("@acesso.usuario(#idUsuario)")
    public long contarNaoLidas(
            Long idUsuario
    ) {

        validarUsuario(idUsuario);

        return notificacaoRepository
                .countByIdUsuarioAndLidaFalse(idUsuario);
    }

    @PreAuthorize("@acesso.notificacao(#idNotificacao)")
    public Notificacao marcarComoLida(
            Long idNotificacao
    ) {

        Notificacao notificacao =
                notificacaoRepository
                        .findById(idNotificacao)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Notificação não encontrada"
                                ));

        if (!notificacao.isLida()) {

            notificacao.setLida(true);
            notificacao.setDataLeitura(
                    LocalDateTime.now()
            );

            notificacaoRepository.save(notificacao);
        }

        return notificacao;
    }

    private void validarUsuario(Long idUsuario) {

        if (!usuarioRepository.existsById(idUsuario)) {
            throw new NoSuchElementException(
                    "Usuário não encontrado"
            );
        }
    }
}
