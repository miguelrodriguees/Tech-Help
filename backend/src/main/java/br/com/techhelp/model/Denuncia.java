package br.com.techhelp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "denuncia")
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_denuncia")
    private Long idDenuncia;

    @Column(name = "id_denunciante", nullable = false)
    private Long idDenunciante;

    @Column(name = "id_usuario_denunciado", nullable = false)
    private Long idUsuarioDenunciado;
    
    @Column(name = "data_resolucao")
    private LocalDateTime dataResolucao;

    @Column(name = "id_servico")
    private Long idServico;

    @Column(nullable = false)
    private String motivo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private String status = "ABERTA";

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

    public Denuncia() {
    }

    public Long getIdDenuncia() {
        return idDenuncia;
    }

    public Long getIdDenunciante() {
        return idDenunciante;
    }

    public void setIdDenunciante(Long idDenunciante) {
        this.idDenunciante = idDenunciante;
    }

    public Long getIdUsuarioDenunciado() {
        return idUsuarioDenunciado;
    }

    public void setIdUsuarioDenunciado(Long idUsuarioDenunciado) {
        this.idUsuarioDenunciado = idUsuarioDenunciado;
    }
    
    public LocalDateTime getDataResolucao() {
        return dataResolucao;
    }

    public void setDataResolucao(LocalDateTime dataResolucao) {
        this.dataResolucao = dataResolucao;
    }

    public Long getIdServico() {
        return idServico;
    }

    public void setIdServico(Long idServico) {
        this.idServico = idServico;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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