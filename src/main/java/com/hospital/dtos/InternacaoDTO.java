package com.hospital.dtos;

import com.hospital.entidades.Internacao;
import com.hospital.entidades.StatusInternacao;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InternacaoDTO {

    private Long id;
    private PacienteDTO paciente;
    private ProfissionalSaudeDTO profissional;
    private QuartoDTO quarto;
    private LocalDateTime dataEntrada;
    private LocalDate dataPrevistaAlta;
    private LocalDateTime dataEfetivaAlta;
    private String observacoes;
    private StatusInternacao status;

    public InternacaoDTO() {
    }

    public InternacaoDTO(Internacao internacao) {
        if (internacao != null) {
            this.id = internacao.getId();
            this.paciente = internacao.getPaciente() != null ? new PacienteDTO(internacao.getPaciente()) : null;
            this.profissional = internacao.getProfissional() != null ? new ProfissionalSaudeDTO(internacao.getProfissional()) : null;
            this.quarto = internacao.getQuarto() != null ? new QuartoDTO(internacao.getQuarto()) : null;
            this.dataEntrada = internacao.getDataEntrada();
            this.dataPrevistaAlta = internacao.getDataPrevistaAlta();
            this.dataEfetivaAlta = internacao.getDataEfetivaAlta();
            this.observacoes = internacao.getObservacoes();
            this.status = internacao.getStatus();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PacienteDTO getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteDTO paciente) {
        this.paciente = paciente;
    }

    public ProfissionalSaudeDTO getProfissional() {
        return profissional;
    }

    public void setProfissional(ProfissionalSaudeDTO profissional) {
        this.profissional = profissional;
    }

    public QuartoDTO getQuarto() {
        return quarto;
    }

    public void setQuarto(QuartoDTO quarto) {
        this.quarto = quarto;
    }

    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDateTime dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public LocalDate getDataPrevistaAlta() {
        return dataPrevistaAlta;
    }

    public void setDataPrevistaAlta(LocalDate dataPrevistaAlta) {
        this.dataPrevistaAlta = dataPrevistaAlta;
    }

    public LocalDateTime getDataEfetivaAlta() {
        return dataEfetivaAlta;
    }

    public void setDataEfetivaAlta(LocalDateTime dataEfetivaAlta) {
        this.dataEfetivaAlta = dataEfetivaAlta;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public StatusInternacao getStatus() {
        return status;
    }

    public void setStatus(StatusInternacao status) {
        this.status = status;
    }
}
