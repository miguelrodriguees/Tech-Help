package br.com.techhelp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacao")
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitacao")
    private Long idSolicitacao;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "id_categoria", nullable = false)
    private Long idCategoria;

    @Column(name = "id_endereco")
    private Long idEndereco;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "tipo_atendimento", nullable = false, length = 20)
    private String tipoAtendimento;

    @Column(nullable = false, length = 20)
    private String urgencia = "NORMAL";

    @Column(name = "orcamento_min", precision = 12, scale = 2)
    private BigDecimal orcamentoMin;

    @Column(name = "orcamento_max", precision = 12, scale = 2)
    private BigDecimal orcamentoMax;

    @Column(nullable = false, length = 25)
    private String status = "ABERTA";

    @Column(name = "data_cadastro", insertable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao", insertable = false, updatable = false)
    private LocalDateTime dataAtualizacao;

    public Solicitacao() {
    }

    public Long getIdSolicitacao() {
        return idSolicitacao;
    }

    public void setIdSolicitacao(Long idSolicitacao) {
        this.idSolicitacao = idSolicitacao;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
    }

    public Long getIdEndereco() {
        return idEndereco;
    }

    public void setIdEndereco(Long idEndereco) {
        this.idEndereco = idEndereco;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipoAtendimento() {
        return tipoAtendimento;
    }

    public void setTipoAtendimento(String tipoAtendimento) {
        this.tipoAtendimento = tipoAtendimento;
    }

    public String getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(String urgencia) {
        this.urgencia = urgencia;
    }

    public BigDecimal getOrcamentoMin() {
        return orcamentoMin;
    }

    public void setOrcamentoMin(BigDecimal orcamentoMin) {
        this.orcamentoMin = orcamentoMin;
    }

    public BigDecimal getOrcamentoMax() {
        return orcamentoMax;
    }

    public void setOrcamentoMax(BigDecimal orcamentoMax) {
        this.orcamentoMax = orcamentoMax;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
}