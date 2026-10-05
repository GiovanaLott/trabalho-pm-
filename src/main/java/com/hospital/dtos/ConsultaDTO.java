package com.hospital.dtos;

import com.hospital.entidades.Consulta;
import com.hospital.entidades.StatusConsulta;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO para transferência e saída de dados de Consulta da API REST.
 *
 * @author Guilherme Oliveira
 */
public class ConsultaDTO {

    private Long id;
    private PacienteDTO paciente;
    private ProfissionalSaudeDTO profissional;
    private LocalDateTime dataHora;
    private LocalDate data;
    private LocalTime horario;
    private String motivoConsulta;
    private String observacoesMedicas;
    private StatusConsulta status;

    public ConsultaDTO() {
    }

    public ConsultaDTO(Long id, PacienteDTO paciente, ProfissionalSaudeDTO profissional, LocalDateTime dataHora, String motivoConsulta, String observacoesMedicas, StatusConsulta status) {
        this.id = id;
        this.paciente = paciente;
        this.profissional = profissional;
        this.dataHora = dataHora;
        this.data = dataHora != null ? dataHora.toLocalDate() : null;
        this.horario = dataHora != null ? dataHora.toLocalTime() : null;
        this.motivoConsulta = motivoConsulta;
        this.observacoesMedicas = observacoesMedicas;
        this.status = status;
    }

    public ConsultaDTO(Consulta consulta) {
        if (consulta != null) {
            this.id = consulta.getId();
            this.paciente = consulta.getPaciente() != null ? new PacienteDTO(consulta.getPaciente()) : null;
            this.profissional = consulta.getProfissional() != null ? new ProfissionalSaudeDTO(consulta.getProfissional()) : null;
            this.dataHora = consulta.getDataHora();
            this.data = consulta.getData();
            this.horario = consulta.getHorario();
            this.motivoConsulta = consulta.getMotivoConsulta();
            this.observacoesMedicas = consulta.getObservacoesMedicas();
            this.status = consulta.getStatus();
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

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
        if (dataHora != null) {
            this.data = dataHora.toLocalDate();
            this.horario = dataHora.toLocalTime();
        }
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public void setHorario(LocalTime horario) {
        this.horario = horario;
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

    public StatusConsulta getStatus() {
        return status;
    }

    public void setStatus(StatusConsulta status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ConsultaDTO{" +
                "id=" + id +
                ", paciente=" + (paciente != null ? paciente.getNome() : null) +
                ", profissional=" + (profissional != null ? profissional.getNome() : null) +
                ", dataHora=" + dataHora +
                ", motivoConsulta='" + motivoConsulta + '\'' +
                ", status=" + status +
                '}';
    }
}
