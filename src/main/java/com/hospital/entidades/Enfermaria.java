package com.hospital.entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Quarto coletivo, de 2 a 8 leitos. */
@Entity
@DiscriminatorValue("ENFERMARIA")
public class Enfermaria extends Quarto {

    public Enfermaria() {
        super();
    }

    public Enfermaria(Long id, String numeroIdentificacao, Integer andar, Integer capacidadeMaxima,
                      Integer ocupacaoAtual, StatusQuarto situacao) {
        super(id, numeroIdentificacao, andar, capacidadeMaxima, ocupacaoAtual, situacao);
    }

    @Override
    public TipoQuarto getTipo() {
        return TipoQuarto.ENFERMARIA;
    }
}
