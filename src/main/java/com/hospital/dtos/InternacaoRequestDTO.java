package com.hospital.dtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para entrada de dados de nova Internação na API REST.
 *
 * @author Guilherme Oliveira
 */
public class InternacaoRequestDTO {

    @NotNull(message = "O ID do paciente é obrigatório")
    private Long pacienteId;

    @NotNull(message = "O ID do profissional responsável é obrigatório")
    private Long profissionalId;

    @NotNull(message = "O ID do quarto é obrigatório")
    private Long quartoId;

    private LocalDateTime dataEntrada;
    private LocalDate dataPrevistaAlta;
    private String observacoes;

    public InternacaoRequestDTO() {
    }

    public InternacaoRequestDTO(Long pacienteId, Long profissionalId, Long quartoId, LocalDateTime dataEntrada, LocalDate dataPrevistaAlta, String observacoes) {
        this.pacienteId = pacienteId;
        this.profissionalId = profissionalId;
        this.quartoId = quartoId;
        this.dataEntrada = dataEntrada != null ? dataEntrada : LocalDateTime.now();
        this.dataPrevistaAlta = dataPrevistaAlta;
        this.observacoes = observacoes;
    }

    public InternacaoRequestDTO(Long pacienteId, Long profissionalId, Long quartoId) {
        this(pacienteId, profissionalId, quartoId, LocalDateTime.now(), null, null);
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

    public Long getQuartoId() {
        return quartoId;
    }

    public void setQuartoId(Long quartoId) {
        this.quartoId = quartoId;
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

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    @Override
    public String toString() {
        return "InternacaoRequestDTO{" +
                "pacienteId=" + pacienteId +
                ", profissionalId=" + profissionalId +
                ", quartoId=" + quartoId +
                ", dataEntrada=" + dataEntrada +
                ", dataPrevistaAlta=" + dataPrevistaAlta +
                '}';
    }
}
