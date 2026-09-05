package br.com.techhelp.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pagamento")
    private Long idPagamento;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;
    
    @Column(name = "id_servico")
    private Long idServico;

    @Column(name = "id_aluguel")
    private Long idAluguel;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "forma_pagamento", nullable = false, length = 30)
    private String formaPagamento;

    @Column(nullable = false, length = 20)
    private String status = "PENDENTE";

    @Column(length = 80)
    private String gateway;

    @Column(name = "id_transacao_externa", unique = true)
    private String idTransacaoExterna;

    @Column(
            name = "data_cadastro",
            insertable = false,
            updatable = false
    )
    private LocalDateTime dataCadastro;

    @Column(
            name = "data_atualizacao",
            insertable = false,
            updatable = false
    )
    private LocalDateTime dataAtualizacao;

    public Pagamento() {
    }

    public Long getIdPagamento() {
        return idPagamento;
    }
    
    public LocalDateTime getDataPagamento() {
    return dataPagamento;
    } 

    public void setDataPagamento(LocalDateTime dataPagamento) {
    this.dataPagamento = dataPagamento;
    }

    public Long getIdServico() {
        return idServico;
    }

    public void setIdServico(Long idServico) {
        this.idServico = idServico;
    }

    public Long getIdAluguel() {
        return idAluguel;
    }

    public void setIdAluguel(Long idAluguel) {
        this.idAluguel = idAluguel;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getIdTransacaoExterna() {
        return idTransacaoExterna;
    }

    public void setIdTransacaoExterna(String idTransacaoExterna) {
        this.idTransacaoExterna = idTransacaoExterna;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
}