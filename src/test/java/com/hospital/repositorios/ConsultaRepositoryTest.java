package com.hospital.repositorios;

import com.hospital.entidades.Consulta;
import com.hospital.entidades.Paciente;
import com.hospital.entidades.ProfissionalSaude;
import com.hospital.entidades.StatusConsulta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa as consultas JPA de {@link ConsultaRepository} contra um banco H2 em memória,
 * com destaque para a query que impede choque de horários do médico.
 */
@DataJpaTest
class ConsultaRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ConsultaRepository consultaRepository;

    private Paciente paciente;
    private ProfissionalSaude medico;
    private ProfissionalSaude outroMedico;
    private LocalDateTime base;

    @BeforeEach
    void setUp() {
        paciente = em.persist(new Paciente(null, "Ana Silva", "111.111.111-11", LocalDate.of(1990, 1, 1),
                "31999990001", "Rua A, 1", "ana@email.com"));
        medico = em.persist(new ProfissionalSaude(null, "Dr. Carlos", "CRM-001", "Cardiologia",
                "31988880001", "carlos@hospital.com"));
        outroMedico = em.persist(new ProfissionalSaude(null, "Dra. Beatriz", "CRM-002", "Pediatria",
                "31988880002", "beatriz@hospital.com"));
        base = LocalDateTime.of(2030, 5, 10, 10, 0);
    }

    private Consulta agendar(ProfissionalSaude profissional, LocalDateTime dataHora, StatusConsulta status) {
        return em.persistAndFlush(new Consulta(null, paciente, profissional, dataHora, "Rotina", null, status));
    }

    private List<Consulta> conflitosPara(ProfissionalSaude profissional, LocalDateTime dataHora) {
        // Mesma janela usada pelo ConsultaService: 29 minutos antes e depois do horário pedido
        return consultaRepository.findConflitoHorarioProfissional(
                profissional.getId(), dataHora.minusMinutes(29), dataHora.plusMinutes(29), StatusConsulta.CANCELADA);
    }

    @Test
    void conflito_encontraConsultaNoMesmoHorario() {
        Consulta existente = agendar(medico, base, StatusConsulta.AGENDADA);

        assertThat(conflitosPara(medico, base))
                .extracting(Consulta::getId)
                .containsExactly(existente.getId());
    }

    @Test
    void conflito_encontraConsultaNoLimiteDaJanela() {
        Consulta existente = agendar(medico, base.plusMinutes(29), StatusConsulta.AGENDADA);

        assertThat(conflitosPara(medico, base))
                .extracting(Consulta::getId)
                .containsExactly(existente.getId());
    }

    @Test
    void conflito_ignoraConsultaForaDaJanela() {
        agendar(medico, base.plusMinutes(30), StatusConsulta.AGENDADA);
        agendar(medico, base.minusMinutes(30), StatusConsulta.AGENDADA);

        assertThat(conflitosPara(medico, base)).isEmpty();
    }

    @Test
    void conflito_ignoraConsultaCancelada() {
        agendar(medico, base, StatusConsulta.CANCELADA);

        assertThat(conflitosPara(medico, base)).isEmpty();
    }

    @Test
    void conflito_ignoraConsultasDeOutroProfissional() {
        agendar(outroMedico, base, StatusConsulta.AGENDADA);

        assertThat(conflitosPara(medico, base)).isEmpty();
    }

    @Test
    void conflito_consultaRealizadaAindaOcupaOHorario() {
        Consulta realizada = agendar(medico, base, StatusConsulta.REALIZADA);

        assertThat(conflitosPara(medico, base))
                .extracting(Consulta::getId)
                .containsExactly(realizada.getId());
    }

    @Test
    void existsPorProfissionalDataHoraEStatus() {
        agendar(medico, base, StatusConsulta.AGENDADA);

        assertThat(consultaRepository.existsByProfissionalIdAndDataHoraAndStatusNot(
                medico.getId(), base, StatusConsulta.CANCELADA)).isTrue();
        assertThat(consultaRepository.existsByProfissionalIdAndDataHoraAndStatusNot(
                medico.getId(), base, StatusConsulta.AGENDADA)).isFalse();
        assertThat(consultaRepository.existsByProfissionalIdAndDataHoraAndStatusNot(
                outroMedico.getId(), base, StatusConsulta.CANCELADA)).isFalse();
    }

    @Test
    void historicoDoPaciente_vemDaMaisRecenteParaAMaisAntiga() {
        Consulta antiga = agendar(medico, base.minusDays(10), StatusConsulta.REALIZADA);
        Consulta recente = agendar(medico, base.plusDays(10), StatusConsulta.AGENDADA);
        Consulta intermediaria = agendar(outroMedico, base, StatusConsulta.AGENDADA);

        assertThat(consultaRepository.findByPacienteIdOrderByDataHoraDesc(paciente.getId()))
                .extracting(Consulta::getId)
                .containsExactly(recente.getId(), intermediaria.getId(), antiga.getId());
    }

    @Test
    void agendaDoProfissional_vemEmOrdemCronologicaESomenteDele() {
        Consulta tarde = agendar(medico, base.plusHours(4), StatusConsulta.AGENDADA);
        Consulta manha = agendar(medico, base, StatusConsulta.AGENDADA);
        agendar(outroMedico, base.plusHours(1), StatusConsulta.AGENDADA);

        assertThat(consultaRepository.findByProfissionalIdOrderByDataHoraAsc(medico.getId()))
                .extracting(Consulta::getId)
                .containsExactly(manha.getId(), tarde.getId());
    }

    @Test
    void consultasDeUmPeriodo_respeitamInicioEFim() {
        Consulta dentro1 = agendar(medico, base, StatusConsulta.AGENDADA);
        Consulta dentro2 = agendar(outroMedico, base.plusHours(2), StatusConsulta.AGENDADA);
        agendar(medico, base.plusDays(1), StatusConsulta.AGENDADA);

        assertThat(consultaRepository.findByDataHoraBetweenOrderByDataHoraAsc(
                base.minusHours(1), base.plusHours(3)))
                .extracting(Consulta::getId)
                .containsExactly(dentro1.getId(), dentro2.getId());
    }

    @Test
    void buscaPorStatus() {
        Consulta agendada = agendar(medico, base, StatusConsulta.AGENDADA);
        agendar(medico, base.plusHours(2), StatusConsulta.CANCELADA);

        assertThat(consultaRepository.findByStatus(StatusConsulta.AGENDADA))
                .extracting(Consulta::getId)
                .containsExactly(agendada.getId());
    }
}
