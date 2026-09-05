package br.com.techhelp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "kit_ferramenta")
@IdClass(KitFerramentaId.class)
public class KitFerramenta {

    @Id
    @Column(name = "id_kit")
    private Long idKit;

    @Id
    @Column(name = "id_ferramenta")
    private Long idFerramenta;

    @Column(nullable = false)
    private Integer quantidade;

    public KitFerramenta() {
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

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
