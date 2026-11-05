package com.hospital.entidades;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfissionalSaudeHierarchyTest {

    @Test
    void deveRepresentarAsTresCategoriasEmUmaUnicaTabela() throws Exception {
        assertTrue(Modifier.isAbstract(ProfissionalSaude.class.getModifiers()),
                "ProfissionalSaude deve ser abstrata");

        Inheritance inheritance = ProfissionalSaude.class.getAnnotation(Inheritance.class);
        assertNotNull(inheritance, "A hierarquia deve declarar a estrategia JPA");
        assertEquals(InheritanceType.SINGLE_TABLE, inheritance.strategy());

        DiscriminatorColumn discriminator = ProfissionalSaude.class.getAnnotation(DiscriminatorColumn.class);
        assertNotNull(discriminator, "A tabela deve distinguir a categoria profissional");
        assertEquals("categoria", discriminator.name());

        assertTrue(ProfissionalSaude.class.isAssignableFrom(
                Class.forName("com.hospital.entidades.ClinicoGeral")));
        assertTrue(ProfissionalSaude.class.isAssignableFrom(
                Class.forName("com.hospital.entidades.Especialista")));
        assertTrue(ProfissionalSaude.class.isAssignableFrom(
                Class.forName("com.hospital.entidades.Cirurgiao")));
    }
}
