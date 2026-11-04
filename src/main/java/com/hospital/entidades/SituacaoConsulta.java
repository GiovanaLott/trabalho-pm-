package com.hospital.entidades;

public enum SituacaoConsulta {
    AGENDADA("Agendada"),
    CONFIRMADA("Confirmada"),
    EM_ATENDIMENTO("Em atendimento"),
    CONCLUIDA("Concluída"),
    CANCELADA("Cancelada"),
    NAO_COMPARECEU("Não compareceu");

    private final String descricao;

    SituacaoConsulta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
