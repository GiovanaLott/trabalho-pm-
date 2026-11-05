package com.hospital.dtos;

import com.hospital.entidades.ModalidadeAtendimento;
import com.hospital.entidades.PrioridadeAtendimento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public class ConsultaRequestDTO {

    @NotNull(message = "O ID do paciente é obrigatório")
    private Long pacienteId;

    @NotNull(message = "O ID do profissional de saúde é obrigatório")
    private Long profissionalId;

    @NotNull(message = "A data e hora da consulta são obrigatórias")
    private LocalDateTime dataHora;

    private String motivoConsulta;

    private String observacoesMedicas;

    private ModalidadeAtendimento modalidade = ModalidadeAtendimento.ROTINA;

    private String especialidade;

    private PrioridadeAtendimento prioridade;

    @Positive(message = "A duração deve ser maior que zero")
    private Integer duracaoMinutos = 30;

    public ConsultaRequestDTO() {
    }

    public ConsultaRequestDTO(Long pacienteId, Long profissionalId, LocalDateTime dataHora,
                              String motivoConsulta, String observacoesMedicas) {
        this.pacienteId = pacienteId;
        this.profissionalId = profissionalId;
        this.dataHora = dataHora;
        this.motivoConsulta = motivoConsulta;
        this.observacoesMedicas = observacoesMedicas;
        this.modalidade = ModalidadeAtendimento.ROTINA;
    }

    public ConsultaRequestDTO(Long pacienteId, Long profissionalId, LocalDateTime dataHora,
                              String motivoConsulta, String observacoesMedicas,
                              ModalidadeAtendimento modalidade, String especialidade,
                              PrioridadeAtendimento prioridade) {
        this.pacienteId = pacienteId;
        this.profissionalId = profissionalId;
        this.dataHora = dataHora;
        this.motivoConsulta = motivoConsulta;
        this.observacoesMedicas = observacoesMedicas;
        this.modalidade = modalidade != null ? modalidade : ModalidadeAtendimento.ROTINA;
        this.especialidade = especialidade;
        this.prioridade = prioridade;
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

    public ModalidadeAtendimento getModalidade() {
        return modalidade;
    }

    public void setModalidade(ModalidadeAtendimento modalidade) {
        this.modalidade = modalidade;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public PrioridadeAtendimento getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(PrioridadeAtendimento prioridade) {
        this.prioridade = prioridade;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }
}
