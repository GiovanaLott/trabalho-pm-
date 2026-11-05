package com.hospital.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Quarto privativo, de 1 a 2 leitos, que pode permitir acompanhante. */
@Entity
@DiscriminatorValue("APARTAMENTO")
public class Apartamento extends Quarto {

    @Column(name = "permite_acompanhante")
    private Boolean permiteAcompanhante = Boolean.TRUE;

    public Apartamento() {
        super();
    }

    public Apartamento(Long id, String numeroIdentificacao, Integer andar, Integer capacidadeMaxima,
                       Integer ocupacaoAtual, StatusQuarto situacao, Boolean permiteAcompanhante) {
        super(id, numeroIdentificacao, andar, capacidadeMaxima, ocupacaoAtual, situacao);
        this.permiteAcompanhante = permiteAcompanhante != null ? permiteAcompanhante : Boolean.TRUE;
    }

    @Override
    public TipoQuarto getTipo() {
        return TipoQuarto.APARTAMENTO;
    }

    public Boolean getPermiteAcompanhante() {
        return permiteAcompanhante;
    }

    public void setPermiteAcompanhante(Boolean permiteAcompanhante) {
        this.permiteAcompanhante = permiteAcompanhante;
    }
}
