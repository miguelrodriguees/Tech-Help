package br.com.techhelp.model;
import java.io.Serializable;
import java.util.Objects;

public class TecnicoEspecialidadeId implements Serializable {

    private Long idTecnico;
    private Long idEspecialidade;

    public TecnicoEspecialidadeId() {
    }

    public TecnicoEspecialidadeId(Long idTecnico, Long idEspecialidade) {
        this.idTecnico = idTecnico;
        this.idEspecialidade = idEspecialidade;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof TecnicoEspecialidadeId)) return false;

        TecnicoEspecialidadeId that = (TecnicoEspecialidadeId) o;

        return Objects.equals(idTecnico, that.idTecnico)
                && Objects.equals(idEspecialidade, that.idEspecialidade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTecnico, idEspecialidade);
    }
}