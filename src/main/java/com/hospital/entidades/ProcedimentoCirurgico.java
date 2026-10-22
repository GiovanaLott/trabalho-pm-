package com.hospital.entidades;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "procedimentos_cirurgicos")
public class ProcedimentoCirurgico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String descricao;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "cirurgiao_id", nullable = false)
    private ProfissionalSaude cirurgiao;

    @Column(name = "data_cirurgia", nullable = false)
    private LocalDate data;

    @Column(name = "horario_inicio_previsto", nullable = false)
    private LocalTime horarioInicioPrevisto;

    @Column(name = "horario_termino")
    private LocalTime horarioTermino;

    @Column(name = "observacoes_pre_operatorias", columnDefinition = "TEXT")
    private String observacoesPreOperatorias;

    @Column(name = "observacoes_pos_operatorias", columnDefinition = "TEXT")
    private String observacoesPosOperatorias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCirurgia situacao = StatusCirurgia.AGENDADA;

    public ProcedimentoCirurgico() {
    }

    public ProcedimentoCirurgico(Long id, String descricao, Paciente paciente, ProfissionalSaude cirurgiao,
                                LocalDate data, LocalTime horarioInicioPrevisto, LocalTime horarioTermino,
                                String observacoesPreOperatorias, String observacoesPosOperatorias,
                                StatusCirurgia situacao) {
        this.id = id;
        this.descricao = descricao;
        this.paciente = paciente;
        this.cirurgiao = cirurgiao;
        this.data = data;
        this.horarioInicioPrevisto = horarioInicioPrevisto;
        this.horarioTermino = horarioTermino;
        this.observacoesPreOperatorias = observacoesPreOperatorias;
        this.observacoesPosOperatorias = observacoesPosOperatorias;
        this.situacao = situacao != null ? situacao : StatusCirurgia.AGENDADA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public ProfissionalSaude getCirurgiao() {
        return cirurgiao;
    }

    public void setCirurgiao(ProfissionalSaude cirurgiao) {
        this.cirurgiao = cirurgiao;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHorarioInicioPrevisto() {
        return horarioInicioPrevisto;
    }

    public void setHorarioInicioPrevisto(LocalTime horarioInicioPrevisto) {
        this.horarioInicioPrevisto = horarioInicioPrevisto;
    }

    public LocalTime getHorarioTermino() {
        return horarioTermino;
    }

    public void setHorarioTermino(LocalTime horarioTermino) {
        this.horarioTermino = horarioTermino;
    }

    public String getObservacoesPreOperatorias() {
        return observacoesPreOperatorias;
    }

    public void setObservacoesPreOperatorias(String observacoesPreOperatorias) {
        this.observacoesPreOperatorias = observacoesPreOperatorias;
    }

    public String getObservacoesPosOperatorias() {
        return observacoesPosOperatorias;
    }

    public void setObservacoesPosOperatorias(String observacoesPosOperatorias) {
        this.observacoesPosOperatorias = observacoesPosOperatorias;
    }

    public StatusCirurgia getSituacao() {
        return situacao;
    }

    public void setSituacao(StatusCirurgia situacao) {
        this.situacao = situacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcedimentoCirurgico that = (ProcedimentoCirurgico) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
