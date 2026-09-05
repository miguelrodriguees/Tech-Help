package br.com.techhelp.model;

import java.io.Serializable;
import java.util.Objects;

public class UsuarioPerfilId implements Serializable {

    private Long idUsuario;
    private Long idPerfil;

    public UsuarioPerfilId() {
    }

    public UsuarioPerfilId(
            Long idUsuario,
            Long idPerfil
    ) {
        this.idUsuario = idUsuario;
        this.idPerfil = idPerfil;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Long getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(Long idPerfil) {
        this.idPerfil = idPerfil;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof UsuarioPerfilId)) {
            return false;
        }

        UsuarioPerfilId that =
                (UsuarioPerfilId) o;

        return Objects.equals(
                idUsuario,
                that.idUsuario
        ) && Objects.equals(
                idPerfil,
                that.idPerfil
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                idUsuario,
                idPerfil
        );
    }
}