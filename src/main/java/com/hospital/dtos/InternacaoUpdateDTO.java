package com.hospital.dtos;

import java.time.LocalDate;

public class InternacaoUpdateDTO {

    private LocalDate dataPrevistaAlta;
    private String observacoes;

    public InternacaoUpdateDTO() {
    }

    public InternacaoUpdateDTO(LocalDate dataPrevistaAlta, String observacoes) {
        this.dataPrevistaAlta = dataPrevistaAlta;
        this.observacoes = observacoes;
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
}
