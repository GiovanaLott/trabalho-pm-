package com.hospital.dtos;

import java.time.LocalTime;
import java.time.LocalDateTime;

public class ConclusaoCirurgiaDTO {

    private LocalTime horarioTermino;
    private LocalDateTime fimEfetivo;
    private String observacoesPosOperatorias;

    public ConclusaoCirurgiaDTO() {
    }

    public ConclusaoCirurgiaDTO(LocalTime horarioTermino, String observacoesPosOperatorias) {
        this.horarioTermino = horarioTermino;
        this.observacoesPosOperatorias = observacoesPosOperatorias;
    }

    public LocalTime getHorarioTermino() {
        return horarioTermino;
    }

    public void setHorarioTermino(LocalTime horarioTermino) {
        this.horarioTermino = horarioTermino;
    }

    public LocalDateTime getFimEfetivo() {
        return fimEfetivo;
    }

    public void setFimEfetivo(LocalDateTime fimEfetivo) {
        this.fimEfetivo = fimEfetivo;
    }

    public String getObservacoesPosOperatorias() {
        return observacoesPosOperatorias;
    }

    public void setObservacoesPosOperatorias(String observacoesPosOperatorias) {
        this.observacoesPosOperatorias = observacoesPosOperatorias;
    }
}
