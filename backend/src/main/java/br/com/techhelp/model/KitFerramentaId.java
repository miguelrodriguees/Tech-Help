package br.com.techhelp.model;

import java.io.Serializable;
import java.util.Objects;

public class KitFerramentaId implements Serializable {

    private Long idKit;
    private Long idFerramenta;

    public KitFerramentaId() {
    }

    public KitFerramentaId(Long idKit, Long idFerramenta) {
        this.idKit = idKit;
        this.idFerramenta = idFerramenta;
    }

    public Long getIdKit() {
        return idKit;
    }

    public void setIdKit(Long idKit) {
        this.idKit = idKit;
    }

    public Long getIdFerramenta() {
        return idFerramenta;
    }

    public void setIdFerramenta(Long idFerramenta) {
        this.idFerramenta = idFerramenta;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof KitFerramentaId)) {
            return false;
        }

        KitFerramentaId that = (KitFerramentaId) o;

        return Objects.equals(idKit, that.idKit)
                && Objects.equals(idFerramenta, that.idFerramenta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idKit, idFerramenta);
    }
}
