package com.hospital.entidades;

import com.hospital.excecoes.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;
import java.util.Objects;

@MappedSuperclass
public abstract class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "profissional_id", nullable = false)
    private ProfissionalSaude profissional;

    @Column(name = "inicio_previsto")
    private LocalDateTime inicioPrevisto;

    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos = 30;

    protected Atendimento() {
    }

    protected Atendimento(Long id, Paciente paciente, ProfissionalSaude profissional,
                          LocalDateTime inicioPrevisto, Integer duracaoMinutos) {
        this.id = id;
        this.paciente = paciente;
        this.profissional = profissional;
        this.inicioPrevisto = inicioPrevisto;
        this.duracaoMinutos = duracaoMinutos != null ? duracaoMinutos : 30;
    }

    public void validarCondicoesAtendimento() {
        if (paciente == null) {
            throw new RegraNegocioException("Paciente é obrigatório para o atendimento.");
        }
        if (profissional == null) {
            throw new RegraNegocioException("Profissional de saúde é obrigatório para o atendimento.");
        }
        if (inicioPrevisto == null) {
            throw new RegraNegocioException("O início previsto do atendimento é obrigatório.");
        }
        if (duracaoMinutos == null || duracaoMinutos <= 0) {
            throw new RegraNegocioException("A duração do atendimento deve ser maior que zero.");
        }
    }

    public LocalDateTime getFimPrevisto() {
        return inicioPrevisto == null || duracaoMinutos == null
                ? null
                : inicioPrevisto.plusMinutes(duracaoMinutos);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public ProfissionalSaude getProfissional() {
        return profissional;
    }

    public void setProfissional(ProfissionalSaude profissional) {
        this.profissional = profissional;
    }

    public LocalDateTime getInicioPrevisto() {
        return inicioPrevisto;
    }

    public void setInicioPrevisto(LocalDateTime inicioPrevisto) {
        this.inicioPrevisto = inicioPrevisto;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Atendimento that = (Atendimento) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
