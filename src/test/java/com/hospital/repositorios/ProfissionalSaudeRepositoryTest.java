package com.hospital.repositorios;

import com.hospital.entidades.ProfissionalSaude;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class ProfissionalSaudeRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProfissionalSaudeRepository profissionalRepository;

    private ProfissionalSaude profissional(String nome, String registro, String especialidade) {
        return em.persistAndFlush(new ProfissionalSaude(null, nome, registro, especialidade,
                "31988880000", "contato@hospital.com"));
    }

    @Test
    void buscaPorRegistroProfissional() {
        ProfissionalSaude carlos = profissional("Dr. Carlos", "CRM-001", "Cardiologia");

        assertThat(profissionalRepository.existsByRegistroProfissional("CRM-001")).isTrue();
        assertThat(profissionalRepository.existsByRegistroProfissional("CRM-999")).isFalse();
        assertThat(profissionalRepository.findByRegistroProfissional("CRM-001"))
                .get()
                .extracting(ProfissionalSaude::getId)
                .isEqualTo(carlos.getId());
    }

    @Test
    void listaPorEspecialidade_ignoraMaiusculasEOrdenaPorNome() {
        profissional("Dr. Zeca", "CRM-003", "Cardiologia");
        profissional("Dra. Beatriz", "CRM-002", "Pediatria");
        profissional("Dr. Carlos", "CRM-001", "Cardiologia");

        assertThat(profissionalRepository.findByEspecialidadeIgnoreCaseOrderByNomeAsc("cardiologia"))
                .extracting(ProfissionalSaude::getNome)
                .containsExactly("Dr. Carlos", "Dr. Zeca");
    }

    @Test
    void registroDuplicado_eRejeitadoPeloBanco() {
        profissionalRepository.saveAndFlush(new ProfissionalSaude(null, "Dr. Carlos", "CRM-001",
                "Cardiologia", null, null));

        assertThrows(DataIntegrityViolationException.class, () ->
                profissionalRepository.saveAndFlush(new ProfissionalSaude(null, "Dra. Outra", "CRM-001",
                        "Pediatria", null, null)));
    }
}
