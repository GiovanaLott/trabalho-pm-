package com.hospital.entidades;

public enum PrioridadeAtendimento {
    BAIXA("Baixa"),
    MODERADA("Moderada"),
    ALTA("Alta"),
    CRITICA("Crítica");

    private final String descricao;

    PrioridadeAtendimento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
