package br.com.techhelp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tecnico_especialidade")
@IdClass(TecnicoEspecialidadeId.class)
public class TecnicoEspecialidade {

    @Id
    @Column(name = "id_tecnico")
    private Long idTecnico;

    @Id
    @Column(name = "id_especialidade")
    private Long idEspecialidade;

    @Column(name = "data_vinculo", insertable = false, updatable = false)
    private LocalDateTime dataVinculo;

    public TecnicoEspecialidade() {
    }

    public Long getIdTecnico() {
        return idTecnico;
    }

    public void setIdTecnico(Long idTecnico) {
        this.idTecnico = idTecnico;
    }

    public Long getIdEspecialidade() {
        return idEspecialidade;
    }

    public void setIdEspecialidade(Long idEspecialidade) {
        this.idEspecialidade = idEspecialidade;
    }

    public LocalDateTime getDataVinculo() {
        return dataVinculo;
    }
}