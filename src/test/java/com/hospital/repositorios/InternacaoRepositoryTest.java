package com.hospital.repositorios;

import com.hospital.entidades.Internacao;
import com.hospital.entidades.Paciente;
import com.hospital.entidades.ProfissionalSaude;
import com.hospital.entidades.Quarto;
import com.hospital.entidades.StatusInternacao;
import com.hospital.entidades.StatusQuarto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InternacaoRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private InternacaoRepository internacaoRepository;

    private Paciente ana;
    private Paciente bruno;
    private ProfissionalSaude medico;
    private Quarto quarto101;
    private Quarto quarto102;
    private LocalDateTime agora;

    @BeforeEach
    void setUp() {
        ana = em.persist(new Paciente(null, "Ana Silva", "111.111.111-11", LocalDate.of(1990, 1, 1),
                null, null, null));
        bruno = em.persist(new Paciente(null, "Bruno Souza", "222.222.222-22", LocalDate.of(1985, 3, 3),
                null, null, null));
        medico = em.persist(new ProfissionalSaude(null, "Dr. Carlos", "CRM-001", "Clinica Geral", null, null));
        quarto101 = em.persist(new Quarto(null, "101", 1, 2, 0, StatusQuarto.DISPONIVEL));
        quarto102 = em.persist(new Quarto(null, "102", 1, 1, 0, StatusQuarto.DISPONIVEL));
        agora = LocalDateTime.of(2030, 1, 15, 8, 0);
    }

    private Internacao internar(Paciente paciente, Quarto quarto, LocalDateTime entrada, StatusInternacao status) {
        return em.persistAndFlush(new Internacao(null, paciente, medico, quarto, entrada,
                entrada.toLocalDate().plusDays(5), null, "Observacao", status));
    }

    @Test
    void pacienteComInternacaoEmAndamento_eDetectado() {
        internar(ana, quarto101, agora, StatusInternacao.EM_ANDAMENTO);

        assertThat(internacaoRepository.existsByPacienteIdAndStatus(ana.getId(), StatusInternacao.EM_ANDAMENTO)).isTrue();
        assertThat(internacaoRepository.existsByPacienteIdAndStatus(bruno.getId(), StatusInternacao.EM_ANDAMENTO)).isFalse();
    }

    @Test
    void internacaoComAlta_naoContaComoEmAndamento() {
        internar(ana, quarto101, agora, StatusInternacao.ALTA);

        assertThat(internacaoRepository.existsByPacienteIdAndStatus(ana.getId(), StatusInternacao.EM_ANDAMENTO)).isFalse();
    }

    @Test
    void contaInternacoesDoQuartoPorStatus() {
        internar(ana, quarto101, agora, StatusInternacao.EM_ANDAMENTO);
        internar(bruno, quarto101, agora, StatusInternacao.EM_ANDAMENTO);
        internar(bruno, quarto101, agora.minusDays(30), StatusInternacao.ALTA);
        internar(ana, quarto102, agora.minusDays(60), StatusInternacao.EM_ANDAMENTO);

        assertThat(internacaoRepository.countByQuartoIdAndStatus(quarto101.getId(), StatusInternacao.EM_ANDAMENTO)).isEqualTo(2);
        assertThat(internacaoRepository.countByQuartoIdAndStatus(quarto101.getId(), StatusInternacao.ALTA)).isEqualTo(1);
        assertThat(internacaoRepository.countByQuartoIdAndStatus(quarto102.getId(), StatusInternacao.EM_ANDAMENTO)).isEqualTo(1);
    }

    @Test
    void listaInternacoesDoQuartoPorStatus() {
        Internacao ativa = internar(ana, quarto101, agora, StatusInternacao.EM_ANDAMENTO);
        internar(bruno, quarto101, agora.minusDays(30), StatusInternacao.ALTA);

        assertThat(internacaoRepository.findByQuartoIdAndStatus(quarto101.getId(), StatusInternacao.EM_ANDAMENTO))
                .extracting(Internacao::getId)
                .containsExactly(ativa.getId());
    }

    @Test
    void historicoDoPaciente_vemDaMaisRecenteParaAMaisAntiga() {
        Internacao antiga = internar(ana, quarto101, agora.minusDays(90), StatusInternacao.ALTA);
        Internacao recente = internar(ana, quarto102, agora, StatusInternacao.EM_ANDAMENTO);
        Internacao intermediaria = internar(ana, quarto101, agora.minusDays(30), StatusInternacao.ALTA);
        internar(bruno, quarto101, agora.minusDays(1), StatusInternacao.ALTA);

        assertThat(internacaoRepository.findByPacienteIdOrderByDataEntradaDesc(ana.getId()))
                .extracting(Internacao::getId)
                .containsExactly(recente.getId(), intermediaria.getId(), antiga.getId());
        assertThat(internacaoRepository.findByPacienteId(ana.getId())).hasSize(3);
    }

    @Test
    void internacaoAtivaDoPaciente_retornaApenasAEmAndamento() {
        internar(ana, quarto101, agora.minusDays(30), StatusInternacao.ALTA);
        Internacao ativa = internar(ana, quarto102, agora, StatusInternacao.EM_ANDAMENTO);

        assertThat(internacaoRepository.findFirstByPacienteIdAndStatus(ana.getId(), StatusInternacao.EM_ANDAMENTO))
                .get()
                .extracting(Internacao::getId)
                .isEqualTo(ativa.getId());
        assertThat(internacaoRepository.findFirstByPacienteIdAndStatus(bruno.getId(), StatusInternacao.EM_ANDAMENTO))
                .isEmpty();
    }

    @Test
    void buscaPorStatus() {
        Internacao ativa = internar(ana, quarto101, agora, StatusInternacao.EM_ANDAMENTO);
        internar(bruno, quarto102, agora, StatusInternacao.ALTA);

        assertThat(internacaoRepository.findByStatus(StatusInternacao.EM_ANDAMENTO))
                .extracting(Internacao::getId)
                .containsExactly(ativa.getId());
    }
}
