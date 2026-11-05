package com.hospital.repositorios;

import com.hospital.entidades.Cirurgiao;
import com.hospital.entidades.ClinicoGeral;
import com.hospital.entidades.Especialista;
import com.hospital.entidades.ProfissionalSaude;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

@DataJpaTest
class ProfissionalSaudeRepositoryTest {

    @Autowired
    private ProfissionalSaudeRepository repository;

    @Test
    void devePersistirAsCategoriasNaMesmaTabela() {
        ProfissionalSaude clinico = repository.saveAndFlush(
                new ClinicoGeral(null, "Dra. Ana", "CRM-100", null, null));
        ProfissionalSaude especialista = repository.saveAndFlush(
                new Especialista(null, "Dr. Bruno", "CRM-200", "Cardiologia", null, null));
        ProfissionalSaude cirurgiao = repository.saveAndFlush(
                new Cirurgiao(null, "Dra. Carla", "CRM-300", "Cirurgia Geral", null, null));

        assertInstanceOf(ClinicoGeral.class, repository.findById(clinico.getId()).orElseThrow());
        assertInstanceOf(Especialista.class, repository.findById(especialista.getId()).orElseThrow());
        assertInstanceOf(Cirurgiao.class, repository.findById(cirurgiao.getId()).orElseThrow());
    }

    @Test
    @SuppressWarnings("unchecked")
    void devePesquisarEspecialistasPorParteDaEspecialidade() throws Exception {
        repository.saveAndFlush(new Especialista(null, "Dr. Bruno", "CRM-201", "Cardiologia", null, null));
        repository.saveAndFlush(new Especialista(null, "Dra. Diana", "CRM-202", "Neurologia", null, null));
        repository.saveAndFlush(new Cirurgiao(null, "Dr. Eduardo", "CRM-203", "Cirurgia cardíaca", null, null));

        Method busca;
        try {
            busca = ProfissionalSaudeRepository.class
                    .getMethod("findEspecialistasByEspecialidadeContainingIgnoreCase", String.class);
        } catch (NoSuchMethodException e) {
            fail("O repositório ainda não oferece pesquisa de especialistas por especialidade");
            return;
        }
        List<ProfissionalSaude> encontrados = (List<ProfissionalSaude>) busca.invoke(repository, "cardio");

        assertEquals(1, encontrados.size());
        assertEquals("Dr. Bruno", encontrados.get(0).getNome());
        assertInstanceOf(Especialista.class, encontrados.get(0));
    }
}
