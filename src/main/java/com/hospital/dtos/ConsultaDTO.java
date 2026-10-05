package com.hospital.dtos;

import com.hospital.entidades.Consulta;
import com.hospital.entidades.StatusConsulta;
import java.time.LocalDateTime;

public class ConsultaDTO {

    private Long id;
    private PacienteDTO paciente;
    private ProfissionalSaudeDTO profissional;
    private LocalDateTime dataHora;
    private String motivoConsulta;
    private String observacoesMedicas;
    private StatusConsulta status;

    public ConsultaDTO() {
    }

    public ConsultaDTO(Consulta consulta) {
        if (consulta != null) {
            this.id = consulta.getId();
            this.paciente = consulta.getPaciente() != null ? new PacienteDTO(consulta.getPaciente()) : null;
            this.profissional = consulta.getProfissional() != null ? new ProfissionalSaudeDTO(consulta.getProfissional()) : null;
            this.dataHora = consulta.getDataHora();
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
}
