package com.hospital.entidades;

/** Tipo de precaução adotada em um quarto de isolamento. */
public enum TipoIsolamento {
    CONTATO("Precaução de contato"),
    GOTICULAS("Precaução para gotículas"),
    AEROSSOIS("Precaução para aerossóis");

    private final String descricao;

    TipoIsolamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
