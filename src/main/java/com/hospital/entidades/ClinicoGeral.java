package com.hospital.entidades;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CLINICO_GERAL")
public class ClinicoGeral extends ProfissionalSaude {

    public ClinicoGeral() {
        super();
    }

    public ClinicoGeral(Long id, String nome, String registroProfissional,
                        String telefone, String email) {
        super(id, nome, registroProfissional, "Clínica Geral", telefone, email);
    }
}
