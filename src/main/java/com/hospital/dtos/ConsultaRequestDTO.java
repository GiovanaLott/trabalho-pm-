package com.hospital.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO para entrada de dados de agendamento de Consulta na API REST.
 *
 * @author Guilherme Oliveira
 */
public class ConsultaRequestDTO {

    @NotNull(message = "O ID do paciente é obrigatório")
    private Long pacienteId;

    @NotNull(message = "O ID do profissional de saúde é obrigatório")
    private Long profissionalId;

    @NotNull(message = "A data e hora da consulta são obrigatórias")
    @FutureOrPresent(message = "A data e hora da consulta devem ser presentes ou futuras")
    private LocalDateTime dataHora;

    @NotBlank(message = "O motivo da consulta é obrigatório")
    private String motivoConsulta;

    private String observacoesMedicas;

    public ConsultaRequestDTO() {
    }

    public ConsultaRequestDTO(Long pacienteId, Long profissionalId, LocalDateTime dataHora, String motivoConsulta, String observacoesMedicas) {
        this.pacienteId = pacienteId;
        this.profissionalId = profissionalId;
        this.dataHora = dataHora;
        this.motivoConsulta = motivoConsulta;
        this.observacoesMedicas = observacoesMedicas;
    }

    public ConsultaRequestDTO(Long pacienteId, Long profissionalId, LocalDateTime dataHora, String motivoConsulta) {
        this(pacienteId, profissionalId, dataHora, motivoConsulta, null);
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Long getProfissionalId() {
        return profissionalId;
    }

    public void setProfissionalId(Long profissionalId) {
        this.profissionalId = profissionalId;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public String getObservacoesMedicas() {
        return observacoesMedicas;
    }

    public void setObservacoesMedicas(String observacoesMedicas) {
        this.observacoesMedicas = observacoesMedicas;
    }

    @Override
    public String toString() {
        return "ConsultaRequestDTO{" +
                "pacienteId=" + pacienteId +
                ", profissionalId=" + profissionalId +
                ", dataHora=" + dataHora +
                ", motivoConsulta='" + motivoConsulta + '\'' +
                '}';
    }
}
