package br.com.techhelp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "aluguel_item")
public class AluguelItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aluguel_item")
    private Long idAluguelItem;

    @Column(name = "id_aluguel", nullable = false)
    private Long idAluguel;

    @Column(name = "id_kit")
    private Long idKit;

    @Column(name = "id_ferramenta")
    private Long idFerramenta;

    public Long getIdFerramenta() { return idFerramenta; }
    public void setIdFerramenta(Long idFerramenta) { this.idFerramenta = idFerramenta; }

    @Column(nullable = false)
    private Integer quantidade = 1;

    @Column(name = "valor_diaria", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorDiaria;

    public AluguelItem() {
    }

    public Long getIdAluguelItem() {
        return idAluguelItem;
    }

    public void setIdAluguelItem(Long idAluguelItem) {
        this.idAluguelItem = idAluguelItem;
    }

    public Long getIdAluguel() {
        return idAluguel;
    }

    public void setIdAluguel(Long idAluguel) {
        this.idAluguel = idAluguel;
    }

    public Long getIdKit() {
        return idKit;
    }

    public void setIdKit(Long idKit) {
        this.idKit = idKit;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public void setValorDiaria(BigDecimal valorDiaria) {
        this.valorDiaria = valorDiaria;
    }
}