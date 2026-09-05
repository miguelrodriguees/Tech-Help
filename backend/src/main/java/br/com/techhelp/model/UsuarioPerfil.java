package br.com.techhelp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_perfil")
@IdClass(UsuarioPerfilId.class)
public class UsuarioPerfil {

    @Id
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Id
    @Column(name = "id_perfil")
    private Long idPerfil;

    @Column(
            name = "data_vinculo",
            insertable = false,
            updatable = false
    )
    private LocalDateTime dataVinculo;

    public UsuarioPerfil() {
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

    public LocalDateTime getDataVinculo() {
        return dataVinculo;
    }
}