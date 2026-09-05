package br.com.techhelp.repository;

import br.com.techhelp.model.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensagemRepository
        extends JpaRepository<Mensagem, Long> {

    List<Mensagem> findByIdConversaOrderByDataEnvioAsc(
            Long idConversa
    );
}