package com.hospital.entidades;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "internacoes", indexes = {
        // Acelera "paciente ja internado?" e a contagem de ocupacao por quarto
        @Index(name = "idx_internacao_paciente_status", columnList = "paciente_id, status"),
        @Index(name = "idx_internacao_quarto_status", columnList = "quarto_id, status")
})
public class Internacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "profissional_id", nullable = false)
    private ProfissionalSaude profissional;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "quarto_id", nullable = false)
    private Quarto quarto;

    @Column(name = "data_entrada", nullable = false)
    private LocalDateTime dataEntrada;

    @Column(name = "data_prevista_alta")
    private LocalDate dataPrevistaAlta;

    @Column(name = "data_efetiva_alta")
    private LocalDateTime dataEfetivaAlta;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusInternacao status = StatusInternacao.EM_ANDAMENTO;

    public Internacao() {
    }

    public Internacao(Long id, Paciente paciente, ProfissionalSaude profissional, Quarto quarto, LocalDateTime dataEntrada, LocalDate dataPrevistaAlta, LocalDateTime dataEfetivaAlta, String observacoes, StatusInternacao status) {
        this.id = id;
        this.paciente = paciente;
        this.profissional = profissional;
        this.quarto = quarto;
        this.dataEntrada = dataEntrada;
        this.dataPrevistaAlta = dataPrevistaAlta;
        this.dataEfetivaAlta = dataEfetivaAlta;
        this.observacoes = observacoes;
        this.status = status != null ? status : StatusInternacao.EM_ANDAMENTO;
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

    public Quarto getQuarto() {
        return quarto;
    }

    public void setQuarto(Quarto quarto) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Internacao internacao = (Internacao) o;
        return Objects.equals(id, internacao.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
