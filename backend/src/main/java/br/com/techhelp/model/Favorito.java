package br.com.techhelp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorito")
@IdClass(FavoritoId.class)
public class Favorito {

    @Id
    @Column(name = "id_cliente")
    private Long idCliente;

    @Id
    @Column(name = "id_tecnico")
    private Long idTecnico;

    @Column(
            name = "data_cadastro",
            insertable = false,
            updatable = false
    )
    private LocalDateTime dataCadastro;

    public Favorito() {
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Long getIdTecnico() {
        return idTecnico;
    }

    public void setIdTecnico(Long idTecnico) {
        this.idTecnico = idTecnico;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
}