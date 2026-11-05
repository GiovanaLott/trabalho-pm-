package com.hospital.repositorios;

import com.hospital.entidades.Paciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PacienteRepositoryTest {

    @Autowired
    private PacienteRepository pacienteRepository;

    private Paciente ana;

    @BeforeEach
    void setUp() {
        ana = pacienteRepository.save(new Paciente(null, "Ana Souza", "529.982.247-25", LocalDate.of(1985, 3, 1), null, null, null));
        pacienteRepository.save(new Paciente(null, "Bruno Lima", "111.444.777-35", LocalDate.of(1970, 7, 9), null, null, null));
        pacienteRepository.save(new Paciente(null, "Mariana Costa", "390.533.447-05", LocalDate.of(2001, 1, 15), null, null, null));
    }

    @Test
    void buscaPorCpfEVerificaExistencia() {
        assertTrue(pacienteRepository.existsByCpf("529.982.247-25"));
        assertEquals("Ana Souza", pacienteRepository.findByCpf("529.982.247-25").orElseThrow().getNome());
        assertFalse(pacienteRepository.existsByCpfAndIdNot("529.982.247-25", ana.getId()));
        assertTrue(pacienteRepository.existsByCpfAndIdNot("111.444.777-35", ana.getId()));
    }

    @Test
    void buscaPorParteDoNomeSemDiferenciarMaiusculas() {
        assertEquals(2, pacienteRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc("ANA").size());
        assertEquals("Ana Souza", pacienteRepository.findAllByOrderByNomeAsc().get(0).getNome());
    }

    @Test
    void bancoImpedeCpfDuplicado() {
        assertThrows(DataIntegrityViolationException.class, () -> pacienteRepository.saveAndFlush(
                new Paciente(null, "Outra Ana", "529.982.247-25", LocalDate.of(1990, 1, 1), null, null, null)));
    }
}
