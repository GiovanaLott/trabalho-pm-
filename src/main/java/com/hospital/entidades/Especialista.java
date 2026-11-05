package com.hospital.entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ESPECIALISTA")
public class Especialista extends ProfissionalSaude {

    public Especialista() {
        super();
    }

    public Especialista(Long id, String nome, String registroProfissional, String especialidade,
                        String telefone, String email) {
        super(id, nome, registroProfissional, especialidade, telefone, email);
    }
}
