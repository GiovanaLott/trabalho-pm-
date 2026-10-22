package com.hospital.dtos;

import com.hospital.entidades.ProcedimentoCirurgico;
import com.hospital.entidades.StatusCirurgia;

import java.time.LocalDate;
import java.time.LocalTime;

public class ProcedimentoCirurgicoDTO {

    private Long id;
    private String descricao;
    private PacienteDTO paciente;
    private ProfissionalSaudeDTO cirurgiao;
    private LocalDate data;
    private LocalTime horarioInicioPrevisto;
    private LocalTime horarioTermino;
    private String observacoesPreOperatorias;
    private String observacoesPosOperatorias;
    private StatusCirurgia situacao;

    public ProcedimentoCirurgicoDTO() {
    }

    public ProcedimentoCirurgicoDTO(Long id, String descricao, PacienteDTO paciente, ProfissionalSaudeDTO cirurgiao,
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
        this.situacao = situacao;
    }

    public ProcedimentoCirurgicoDTO(ProcedimentoCirurgico procedimento) {
        if (procedimento != null) {
            this.id = procedimento.getId();
            this.descricao = procedimento.getDescricao();
            this.paciente = procedimento.getPaciente() != null ? new PacienteDTO(procedimento.getPaciente()) : null;
            this.cirurgiao = procedimento.getCirurgiao() != null ? new ProfissionalSaudeDTO(procedimento.getCirurgiao()) : null;
            this.data = procedimento.getData();
            this.horarioInicioPrevisto = procedimento.getHorarioInicioPrevisto();
            this.horarioTermino = procedimento.getHorarioTermino();
            this.observacoesPreOperatorias = procedimento.getObservacoesPreOperatorias();
            this.observacoesPosOperatorias = procedimento.getObservacoesPosOperatorias();
            this.situacao = procedimento.getSituacao();
        }
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

    public PacienteDTO getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteDTO paciente) {
        this.paciente = paciente;
    }

    public ProfissionalSaudeDTO getCirurgiao() {
        return cirurgiao;
    }

    public void setCirurgiao(ProfissionalSaudeDTO cirurgiao) {
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
}
