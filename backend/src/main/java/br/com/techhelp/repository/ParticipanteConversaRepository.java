package br.com.techhelp.repository;

import br.com.techhelp.model.ParticipanteConversa;
import br.com.techhelp.model.ParticipanteConversaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipanteConversaRepository
        extends JpaRepository<
                ParticipanteConversa,
                ParticipanteConversaId
        > {

    boolean existsByIdConversaAndIdUsuario(
            Long idConversa,
            Long idUsuario
    );

    List<ParticipanteConversa> findByIdUsuario(
            Long idUsuario
    );

    List<ParticipanteConversa> findByIdConversa(
            Long idConversa
    );
}