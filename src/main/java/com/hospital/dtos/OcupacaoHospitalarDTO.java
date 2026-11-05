package com.hospital.dtos;

import com.hospital.entidades.TipoQuarto;

import java.util.List;

/**
 * Painel de ocupação dos quartos: totais gerais e por tipo de quarto.
 * Leitos de quartos em manutenção não contam como disponíveis.
 */
public record OcupacaoHospitalarDTO(
        int totalQuartos,
        int quartosEmManutencao,
        int totalLeitos,
        int leitosOcupados,
        int leitosDisponiveis,
        double taxaOcupacaoPercentual,
        List<PorTipo> porTipo) {

    public record PorTipo(
            TipoQuarto tipo,
            String descricao,
            int quartos,
            int totalLeitos,
            int leitosOcupados,
            int leitosDisponiveis) {
    }
}
