package br.com.techhelp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "participante_conversa")
@IdClass(ParticipanteConversaId.class)
public class ParticipanteConversa {

    @Id
    @Column(name = "id_conversa")
    private Long idConversa;

    @Id
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(
            name = "data_entrada",
            insertable = false,
            updatable = false
    )
    private LocalDateTime dataEntrada;

    @Column(name = "ultimo_acesso_em")
    private LocalDateTime ultimoAcessoEm;

    public ParticipanteConversa() {
    }

    public Long getIdConversa() {
        return idConversa;
    }

    public void setIdConversa(Long idConversa) {
        this.idConversa = idConversa;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }

    public LocalDateTime getUltimoAcessoEm() {
        return ultimoAcessoEm;
    }

    public void setUltimoAcessoEm(LocalDateTime ultimoAcessoEm) {
        this.ultimoAcessoEm = ultimoAcessoEm;
    }
}