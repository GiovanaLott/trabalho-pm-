package com.hospital.entidades;

import jakarta.persistence.MappedSuperclass;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class AtendimentoHierarchyTest {

    @Test
    void deveCompartilharOMapeamentoSemUnificarAsTabelasConcretas() {
        Class<?> atendimento;
        try {
            atendimento = Class.forName("com.hospital.entidades.Atendimento");
        } catch (ClassNotFoundException e) {
            fail("A classe-base Atendimento ainda não foi criada");
            return;
        }

        assertTrue(Modifier.isAbstract(atendimento.getModifiers()));
        assertNotNull(atendimento.getAnnotation(MappedSuperclass.class));
        assertTrue(atendimento.isAssignableFrom(Consulta.class));
        assertTrue(atendimento.isAssignableFrom(ProcedimentoCirurgico.class));
    }
}
