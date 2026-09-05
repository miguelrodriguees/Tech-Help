package br.com.techhelp.service;

import br.com.techhelp.dto.EnviarMensagemRequest;
import br.com.techhelp.model.Conversa;
import br.com.techhelp.model.Mensagem;
import br.com.techhelp.model.ParticipanteConversa;
import br.com.techhelp.model.ParticipanteConversaId;
import br.com.techhelp.repository.ConversaRepository;
import br.com.techhelp.repository.MensagemRepository;
import br.com.techhelp.repository.ParticipanteConversaRepository;
import br.com.techhelp.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MensagemService {

    private final MensagemRepository mensagemRepository;
    private final ConversaRepository conversaRepository;
    private final ParticipanteConversaRepository participanteRepository;
    private final UsuarioRepository usuarioRepository;

    public MensagemService(
            MensagemRepository mensagemRepository,
            ConversaRepository conversaRepository,
            ParticipanteConversaRepository participanteRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.mensagemRepository = mensagemRepository;
        this.conversaRepository = conversaRepository;
        this.participanteRepository = participanteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Mensagem enviar(
            Long idConversa,
            EnviarMensagemRequest request
    ) {

        Conversa conversa = conversaRepository
                .findById(idConversa)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Conversa não encontrada"
                        ));

        if (!"ATIVA".equals(conversa.getStatus())) {
            throw new IllegalArgumentException(
                    "Esta conversa não está ativa"
            );
        }

        if (!usuarioRepository
                .existsById(request.idUsuario())) {

            throw new NoSuchElementException(
                    "Usuário não encontrado"
            );
        }

        if (!participanteRepository
                .existsByIdConversaAndIdUsuario(
                        idConversa,
                        request.idUsuario()
                )) {

            throw new IllegalArgumentException(
                    "Este usuário não participa da conversa"
            );
        }

        String tipo = request.tipo();

        if (tipo == null || tipo.isBlank()) {
            tipo = "TEXTO";
        }

        tipo = tipo.toUpperCase();

        if (!tipo.equals("TEXTO")
                && !tipo.equals("IMAGEM")
                && !tipo.equals("ARQUIVO")
                && !tipo.equals("SISTEMA")) {

            throw new IllegalArgumentException(
                    "Tipo de mensagem inválido"
            );
        }

        boolean semTexto =
                request.texto() == null
                || request.texto().isBlank();

        boolean semArquivo =
                request.arquivoUrl() == null
                || request.arquivoUrl().isBlank();

        if (semTexto && semArquivo) {
            throw new IllegalArgumentException(
                    "A mensagem precisa possuir texto ou arquivo"
            );
        }

        Mensagem mensagem = new Mensagem();

        mensagem.setIdConversa(idConversa);
        mensagem.setIdUsuario(request.idUsuario());
        mensagem.setTipo(tipo);
        mensagem.setTexto(request.texto());
        mensagem.setArquivoUrl(request.arquivoUrl());

        return mensagemRepository.save(mensagem);
    }

    public List<Mensagem> listar(
            Long idConversa,
            Long idUsuario
    ) {

        if (!conversaRepository.existsById(idConversa)) {
            throw new NoSuchElementException(
                    "Conversa não encontrada"
            );
        }

        if (!participanteRepository
                .existsByIdConversaAndIdUsuario(
                        idConversa,
                        idUsuario
                )) {

            throw new IllegalArgumentException(
                    "Este usuário não participa da conversa"
            );
        }

        ParticipanteConversaId participanteId =
                new ParticipanteConversaId(
                        idConversa,
                        idUsuario
                );

        ParticipanteConversa participante =
                participanteRepository
                        .findById(participanteId)
                        .orElseThrow();

        participante.setUltimoAcessoEm(
                LocalDateTime.now()
        );

        participanteRepository.save(participante);

        return mensagemRepository
                .findByIdConversaOrderByDataEnvioAsc(
                        idConversa
                );
    }
}