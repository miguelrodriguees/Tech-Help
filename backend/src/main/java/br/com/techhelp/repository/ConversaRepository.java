package br.com.techhelp.repository;

import br.com.techhelp.model.Conversa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversaRepository
        extends JpaRepository<Conversa, Long> {
}