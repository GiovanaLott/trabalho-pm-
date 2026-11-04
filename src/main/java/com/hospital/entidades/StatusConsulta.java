package com.hospital.entidades;

public enum StatusConsulta {
    AGENDADA,
    CONFIRMADA,
    EM_ATENDIMENTO,
    CONCLUIDA,
    CANCELADA,
    NAO_COMPARECEU,
    @Deprecated
    REALIZADA
}
