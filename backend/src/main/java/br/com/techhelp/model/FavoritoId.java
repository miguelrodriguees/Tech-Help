package br.com.techhelp.model;

import java.io.Serializable;
import java.util.Objects;

public class FavoritoId implements Serializable {

    private Long idCliente;
    private Long idTecnico;

    public FavoritoId() {
    }

    public FavoritoId(Long idCliente, Long idTecnico) {
        this.idCliente = idCliente;
        this.idTecnico = idTecnico;
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

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof FavoritoId)) {
            return false;
        }

        FavoritoId favoritoId = (FavoritoId) o;

        return Objects.equals(idCliente, favoritoId.idCliente)
                && Objects.equals(idTecnico, favoritoId.idTecnico);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCliente, idTecnico);
    }
}