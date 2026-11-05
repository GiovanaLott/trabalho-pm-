package com.hospital.entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CIRURGIAO")
public class Cirurgiao extends ProfissionalSaude {

    public Cirurgiao() {
        super();
    }

    public Cirurgiao(Long id, String nome, String registroProfissional, String especialidade,
                     String telefone, String email) {
        super(id, nome, registroProfissional, especialidade, telefone, email);
    }
}
