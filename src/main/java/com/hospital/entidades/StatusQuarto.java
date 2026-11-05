package com.hospital.entidades;

/**
 * Situação atual do quarto. DISPONIVEL e OCUPADO são calculadas automaticamente a partir da
 * ocupação; EM_MANUTENCAO é definida manualmente e bloqueia novas internações.
 */
public enum StatusQuarto {
    DISPONIVEL,
    OCUPADO,
    EM_MANUTENCAO
}
