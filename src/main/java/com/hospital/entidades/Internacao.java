package com.hospital.entidades;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa a entidade e modelo de domínio de uma Internação hospitalar.
 * Em conformidade com os requisitos mínimos:
 * - Paciente
 * - Profissional responsável
 * - Quarto
 * - Data de entrada
 * - Data prevista de alta
 * - Data efetiva de alta
 * - Observações
 *
 * @author Guilherme Oliveira
 */
@Entity
@Table(name = "internacoes")
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

    /**
     * Construtor padrão (obrigatório para JPA).
     */
    public Internacao() {
    }

    /**
     * Construtor completo com ID.
     */
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

    /**
     * Construtor sem ID para nova internação em andamento.
     */
    public Internacao(Paciente paciente, ProfissionalSaude profissional, Quarto quarto, LocalDateTime dataEntrada, LocalDate dataPrevistaAlta, String observacoes) {
        this(null, paciente, profissional, quarto, dataEntrada, dataPrevistaAlta, null, observacoes, StatusInternacao.EM_ANDAMENTO);
    }

    // --- Métodos de Domínio (Java puro / POO / Regras de Negócio) ---

    /**
     * Registra a alta hospitalar do paciente, finalizando a internação.
     *
     * @param dataAlta Data e hora em que a alta foi concedida.
     * @param observacoesAdicionais Parecer médico de encerramento da internação.
     */
    public void darAlta(LocalDateTime dataAlta, String observacoesAdicionais) {
        this.dataEfetivaAlta = dataAlta != null ? dataAlta : LocalDateTime.now();
        this.status = StatusInternacao.ALTA;
        if (observacoesAdicionais != null && !observacoesAdicionais.trim().isEmpty()) {
            if (this.observacoes == null || this.observacoes.trim().isEmpty()) {
                this.observacoes = observacoesAdicionais;
            } else {
                this.observacoes += " | Alta: " + observacoesAdicionais;
            }
        }
    }

    /**
     * Registra a alta hospitalar com a data/hora atual.
     */
    public void darAlta() {
        darAlta(LocalDateTime.now(), null);
    }

    /**
     * Calcula o total de dias em que o paciente esteve/está internado.
     *
     * @return Quantidade de dias de permanência.
     */
    public long calcularDiasInternacao() {
        if (this.dataEntrada == null) return 0;
        LocalDateTime fim = (this.dataEfetivaAlta != null) ? this.dataEfetivaAlta : LocalDateTime.now();
        return Math.max(1, Duration.between(this.dataEntrada, fim).toDays());
    }

    public boolean isEmAndamento() {
        return StatusInternacao.EM_ANDAMENTO.equals(this.status);
    }

    public boolean isAlta() {
        return StatusInternacao.ALTA.equals(this.status);
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
        Internacao that = (Internacao) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Internacao{" +
                "id=" + id +
                ", paciente=" + (paciente != null ? paciente.getNome() : null) +
                ", profissional=" + (profissional != null ? profissional.getNome() : null) +
                ", quarto=" + (quarto != null ? quarto.getNumeroIdentificacao() : null) +
                ", dataEntrada=" + dataEntrada +
                ", dataPrevistaAlta=" + dataPrevistaAlta +
                ", dataEfetivaAlta=" + dataEfetivaAlta +
                ", status=" + status +
                '}';
    }
}
