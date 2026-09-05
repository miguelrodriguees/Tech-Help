package br.com.techhelp.repository;

import br.com.techhelp.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository
        extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByIdUsuarioOrderByDataCadastroDesc(
            Long idUsuario
    );

    List<Notificacao> findByIdUsuarioAndLidaFalseOrderByDataCadastroDesc(
            Long idUsuario
    );

    long countByIdUsuarioAndLidaFalse(
            Long idUsuario
    );
}