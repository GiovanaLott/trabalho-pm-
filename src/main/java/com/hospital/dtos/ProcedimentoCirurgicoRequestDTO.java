package com.hospital.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class ProcedimentoCirurgicoRequestDTO {

    @NotBlank(message = "A descrição do procedimento é obrigatória")
    private String descricao;

    @NotBlank(message = "O tipo do procedimento é obrigatório")
    private String tipo;

    @NotNull(message = "O ID do paciente é obrigatório")
    private Long pacienteId;

    @NotNull(message = "O ID do cirurgião responsável é obrigatório")
    private Long cirurgiaoId;

    @NotNull(message = "A data da cirurgia é obrigatória")
    private LocalDate data;

    @NotNull(message = "O horário de início previsto é obrigatório")
    private LocalTime horarioInicioPrevisto;

    private LocalTime horarioFimPrevisto;

    private String observacoesPreOperatorias;

    public ProcedimentoCirurgicoRequestDTO() {
    }

    public ProcedimentoCirurgicoRequestDTO(String descricao, Long pacienteId, Long cirurgiaoId,
                                         LocalDate data, LocalTime horarioInicioPrevisto,
                                         String observacoesPreOperatorias) {
        this.descricao = descricao;
        this.pacienteId = pacienteId;
        this.cirurgiaoId = cirurgiaoId;
        this.data = data;
        this.horarioInicioPrevisto = horarioInicioPrevisto;
        this.observacoesPreOperatorias = observacoesPreOperatorias;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Long getCirurgiaoId() {
        return cirurgiaoId;
    }

    public void setCirurgiaoId(Long cirurgiaoId) {
        this.cirurgiaoId = cirurgiaoId;
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

    public LocalTime getHorarioFimPrevisto() {
        return horarioFimPrevisto;
    }

    public void setHorarioFimPrevisto(LocalTime horarioFimPrevisto) {
        this.horarioFimPrevisto = horarioFimPrevisto;
    }

    public String getObservacoesPreOperatorias() {
        return observacoesPreOperatorias;
    }

    public void setObservacoesPreOperatorias(String observacoesPreOperatorias) {
        this.observacoesPreOperatorias = observacoesPreOperatorias;
    }
}
