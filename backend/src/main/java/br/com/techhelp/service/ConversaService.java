package br.com.techhelp.service;

import br.com.techhelp.dto.CriarConversaRequest;
import br.com.techhelp.model.Conversa;
import br.com.techhelp.model.ParticipanteConversa;
import br.com.techhelp.repository.ConversaRepository;
import br.com.techhelp.repository.ParticipanteConversaRepository;
import br.com.techhelp.repository.SolicitacaoRepository;
import br.com.techhelp.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ConversaService {

    private final ConversaRepository conversaRepository;
    private final ParticipanteConversaRepository participanteRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public ConversaService(
            ConversaRepository conversaRepository,
            ParticipanteConversaRepository participanteRepository,
            SolicitacaoRepository solicitacaoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.conversaRepository = conversaRepository;
        this.participanteRepository = participanteRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Conversa criar(
            CriarConversaRequest request
    ) {

        if (!solicitacaoRepository
                .existsById(request.idSolicitacao())) {

            throw new NoSuchElementException(
                    "Solicitação não encontrada"
            );
        }

        if (!usuarioRepository
                .existsById(request.idUsuario1())) {

            throw new NoSuchElementException(
                    "Primeiro usuário não encontrado"
            );
        }

        if (!usuarioRepository
                .existsById(request.idUsuario2())) {

            throw new NoSuchElementException(
                    "Segundo usuário não encontrado"
            );
        }

        if (request.idUsuario1()
                .equals(request.idUsuario2())) {

            throw new IllegalArgumentException(
                    "A conversa precisa ter dois usuários diferentes"
            );
        }

        Conversa conversa = new Conversa();

        conversa.setIdSolicitacao(
                request.idSolicitacao()
        );

        conversa.setStatus("ATIVA");

        conversa = conversaRepository.save(conversa);

        ParticipanteConversa participante1 =
                new ParticipanteConversa();

        participante1.setIdConversa(
                conversa.getIdConversa()
        );

        participante1.setIdUsuario(
                request.idUsuario1()
        );

        participanteRepository.save(participante1);

        ParticipanteConversa participante2 =
                new ParticipanteConversa();

        participante2.setIdConversa(
                conversa.getIdConversa()
        );

        participante2.setIdUsuario(
                request.idUsuario2()
        );

        participanteRepository.save(participante2);

        return conversa;
    }

    public Conversa buscarPorId(Long idConversa) {

        return conversaRepository
                .findById(idConversa)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Conversa não encontrada"
                        ));
    }

    public List<Conversa> listarPorUsuario(
            Long idUsuario
    ) {

        if (!usuarioRepository.existsById(idUsuario)) {
            throw new NoSuchElementException(
                    "Usuário não encontrado"
            );
        }

        return participanteRepository
                .findByIdUsuario(idUsuario)
                .stream()
                .map(participante ->
                        conversaRepository
                                .findById(
                                        participante.getIdConversa()
                                )
                                .orElseThrow()
                )
                .toList();
    }
}