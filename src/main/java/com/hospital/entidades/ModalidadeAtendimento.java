package com.hospital.entidades;

public enum ModalidadeAtendimento {
    ROTINA("Consulta de rotina"),
    ESPECIALIZADA("Consulta especializada"),
    EMERGENCIA("Atendimento de emergência");

    private final String descricao;

    ModalidadeAtendimento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
