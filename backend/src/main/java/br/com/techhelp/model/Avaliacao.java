package br.com.techhelp.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacao")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avaliacao")
    private Long idAvaliacao;

    @Column(name = "id_servico", nullable = false)
    private Long idServico;

    @Column(name = "id_avaliador", nullable = false)
    private Long idAvaliador;

    @Column(name = "id_avaliado", nullable = false)
    private Long idAvaliado;

    @Column(nullable = false)
    private Byte nota;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(
            name = "data_avaliacao",
            insertable = false,
            updatable = false
    )
    private LocalDateTime dataAvaliacao;

    public Avaliacao() {
    }

    public Long getIdAvaliacao() {
        return idAvaliacao;
    }

    public void setIdAvaliacao(Long idAvaliacao) {
        this.idAvaliacao = idAvaliacao;
    }

    public Long getIdServico() {
        return idServico;
    }

    public void setIdServico(Long idServico) {
        this.idServico = idServico;
    }

    public Long getIdAvaliador() {
        return idAvaliador;
    }

    public void setIdAvaliador(Long idAvaliador) {
        this.idAvaliador = idAvaliador;
    }

    public Long getIdAvaliado() {
        return idAvaliado;
    }

    public void setIdAvaliado(Long idAvaliado) {
        this.idAvaliado = idAvaliado;
    }

    public Byte getNota() {
        return nota;
    }

    public void setNota(Byte nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getDataAvaliacao() {
        return dataAvaliacao;
    }
}