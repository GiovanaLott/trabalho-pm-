package com.hospital.dtos;

import java.time.LocalTime;

public class ConclusaoCirurgiaDTO {

    private LocalTime horarioTermino;
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

    public String getObservacoesPosOperatorias() {
        return observacoesPosOperatorias;
    }

    public void setObservacoesPosOperatorias(String observacoesPosOperatorias) {
        this.observacoesPosOperatorias = observacoesPosOperatorias;
    }
}
