package com.hospital.repositorios;

import com.hospital.entidades.Paciente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class PacienteRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PacienteRepository pacienteRepository;

    private Paciente paciente(String nome, String cpf) {
        return em.persistAndFlush(new Paciente(null, nome, cpf, LocalDate.of(1990, 1, 1),
                "31999990000", "Rua A", nome.split(" ")[0].toLowerCase() + "@email.com"));
    }

    @Test
    void buscaPorCpf() {
        Paciente ana = paciente("Ana Silva", "111.111.111-11");

        assertThat(pacienteRepository.existsByCpf("111.111.111-11")).isTrue();
        assertThat(pacienteRepository.existsByCpf("000.000.000-00")).isFalse();
        assertThat(pacienteRepository.findByCpf("111.111.111-11"))
                .get()
                .extracting(Paciente::getId)
                .isEqualTo(ana.getId());
        assertThat(pacienteRepository.findByCpf("000.000.000-00")).isEmpty();
    }

    @Test
    void buscaPorParteDoNome_ignoraMaiusculasEOrdenaAlfabeticamente() {
        paciente("Carlos Souza", "333.333.333-33");
        paciente("Bruno Silveira", "222.222.222-22");
        paciente("Ana Silva", "111.111.111-11");

        assertThat(pacienteRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc("SIL"))
                .extracting(Paciente::getNome)
                .containsExactly("Ana Silva", "Bruno Silveira");
        assertThat(pacienteRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc("xyz")).isEmpty();
    }

    @Test
    void cpfDuplicado_eRejeitadoPeloBanco() {
        pacienteRepository.saveAndFlush(new Paciente(null, "Ana Silva", "111.111.111-11",
                LocalDate.of(1990, 1, 1), null, null, null));

        assertThrows(DataIntegrityViolationException.class, () ->
                pacienteRepository.saveAndFlush(new Paciente(null, "Outra Pessoa", "111.111.111-11",
                        LocalDate.of(1980, 2, 2), null, null, null)));
    }

    @Test
    void camposObrigatoriosAusentes_saoRejeitadosPeloBanco() {
        assertThrows(DataIntegrityViolationException.class, () ->
                pacienteRepository.saveAndFlush(new Paciente(null, null, "444.444.444-44",
                        LocalDate.of(1990, 1, 1), null, null, null)));
    }
}
