package br.com.techhelp.model;

import java.io.Serializable;
import java.util.Objects;

public class ParticipanteConversaId implements Serializable {

    private Long idConversa;
    private Long idUsuario;

    public ParticipanteConversaId() {
    }

    public ParticipanteConversaId(Long idConversa, Long idUsuario) {
        this.idConversa = idConversa;
        this.idUsuario = idUsuario;
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

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof ParticipanteConversaId)) {
            return false;
        }

        ParticipanteConversaId that =
                (ParticipanteConversaId) o;

        return Objects.equals(idConversa, that.idConversa)
                && Objects.equals(idUsuario, that.idUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idConversa, idUsuario);
    }
}