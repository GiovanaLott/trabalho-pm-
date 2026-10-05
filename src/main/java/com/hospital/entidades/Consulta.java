package com.hospital.entidades;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Representa a entidade e modelo de domínio de uma Consulta médica.
 * Em conformidade com os requisitos mínimos:
 * - Paciente
 * - Profissional responsável
 * - Data e Horário
 * - Motivo da consulta
 * - Observações médicas
 *
 * @author Guilherme Oliveira
 */
@Entity
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "profissional_id", nullable = false)
    private ProfissionalSaude profissional;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "motivo_consulta", nullable = false, length = 255)
    private String motivoConsulta;

    @Column(name = "observacoes_medicas", columnDefinition = "TEXT")
    private String observacoesMedicas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConsulta status = StatusConsulta.AGENDADA;

    /**
     * Construtor padrão (obrigatório para JPA).
     */
    public Consulta() {
    }

    /**
     * Construtor completo com ID.
     */
    public Consulta(Long id, Paciente paciente, ProfissionalSaude profissional, LocalDateTime dataHora, String motivoConsulta, String observacoesMedicas, StatusConsulta status) {
        this.id = id;
        this.paciente = paciente;
        this.profissional = profissional;
        this.dataHora = dataHora;
        this.motivoConsulta = motivoConsulta;
        this.observacoesMedicas = observacoesMedicas;
        this.status = status != null ? status : StatusConsulta.AGENDADA;
    }

    /**
     * Construtor sem ID para agendamento de nova consulta.
     */
    public Consulta(Paciente paciente, ProfissionalSaude profissional, LocalDateTime dataHora, String motivoConsulta) {
        this(null, paciente, profissional, dataHora, motivoConsulta, null, StatusConsulta.AGENDADA);
    }

    // --- Métodos de Domínio (Java puro / POO / Regras de Negócio) ---

    /**
     * Cancela a consulta agendada.
     */
    public void cancelar() {
        this.status = StatusConsulta.CANCELADA;
    }

    /**
     * Finaliza a consulta médica, registrando as observações clínicas.
     *
     * @param observacoes Parecer médico e orientações registradas no atendimento.
     */
    public void finalizar(String observacoes) {
        this.status = StatusConsulta.REALIZADA;
        if (observacoes != null && !observacoes.trim().isEmpty()) {
            this.observacoesMedicas = observacoes;
        }
    }

    /**
     * Retorna apenas a data da consulta.
     */
    public LocalDate getData() {
        return this.dataHora != null ? this.dataHora.toLocalDate() : null;
    }

    /**
     * Retorna apenas o horário da consulta.
     */
    public LocalTime getHorario() {
        return this.dataHora != null ? this.dataHora.toLocalTime() : null;
    }

    public boolean isAgendada() {
        return StatusConsulta.AGENDADA.equals(this.status);
    }

    public boolean isRealizada() {
        return StatusConsulta.REALIZADA.equals(this.status);
    }

    public boolean isCancelada() {
        return StatusConsulta.CANCELADA.equals(this.status);
    }

    // --- Getters e Setters ---

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Consulta consulta = (Consulta) o;
        return Objects.equals(id, consulta.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Consulta{" +
                "id=" + id +
                ", paciente=" + (paciente != null ? paciente.getNome() : null) +
                ", profissional=" + (profissional != null ? profissional.getNome() : null) +
                ", dataHora=" + dataHora +
                ", motivoConsulta='" + motivoConsulta + '\'' +
                ", status=" + status +
                '}';
    }
}
