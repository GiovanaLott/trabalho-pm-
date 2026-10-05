package com.hospital.entidades;

import com.hospital.dtos.HistoricoMedicoDTO;
import com.hospital.dtos.PacienteDTO;
import com.hospital.dtos.ProfissionalSaudeDTO;
import com.hospital.dtos.QuartoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ModelosTest {

    @Test
    @DisplayName("Deve validar metodos de dominio de Paciente")
    public void testPacienteDominio() {
        LocalDate nascimento = LocalDate.now().minusYears(25);
        Paciente paciente = new Paciente(1L, "Carlos Andrade", "111.222.333-44", nascimento, "31988887777", "Rua das Flores, 100", "carlos@email.com");

        assertEquals(25, paciente.getIdade());
        assertTrue(paciente.isMaiorDeIdade());
        assertNotNull(paciente.toString());
        assertEquals("Carlos Andrade", paciente.getNome());

        PacienteDTO dto = new PacienteDTO(paciente);
        Paciente deDto = dto.paraEntidade();
        assertEquals(paciente.getCpf(), deDto.getCpf());
    }

    @Test
    @DisplayName("Deve validar metodos de dominio de ProfissionalSaude")
    public void testProfissionalSaudeDominio() {
        ProfissionalSaude prof = new ProfissionalSaude(2L, "Dra. Ana Paula", "CRM-MG 98765", "Pediatria", "31977776666", "ana@hospital.com");

        assertTrue(prof.temRegistroValido());
        assertEquals("Dra. Ana Paula - Pediatria (CRM-MG 98765)", prof.getIdentificacaoFormatada());
        assertNotNull(prof.toString());

        ProfissionalSaudeDTO dto = new ProfissionalSaudeDTO(prof);
        ProfissionalSaude deDto = dto.paraEntidade();
        assertEquals(prof.getRegistroProfissional(), deDto.getRegistroProfissional());
    }

    @Test
    @DisplayName("Deve validar capacidade e ocupacao de Quarto")
    public void testQuartoDominio() {
        Quarto quarto = new Quarto(10L, "205-B", 2, 2, 0, StatusQuarto.DISPONIVEL);

        assertTrue(quarto.temVagaDisponivel());
        assertEquals(2, quarto.getVagasDisponiveis());
        assertTrue(quarto.isDisponivel());
        assertFalse(quarto.isOcupado());

        quarto.incrementarOcupacao();
        assertEquals(1, quarto.getOcupacaoAtual());
        assertEquals(1, quarto.getVagasDisponiveis());
        assertEquals(StatusQuarto.DISPONIVEL, quarto.getSituacao());

        quarto.incrementarOcupacao();
        assertEquals(2, quarto.getOcupacaoAtual());
        assertEquals(0, quarto.getVagasDisponiveis());
        assertEquals(StatusQuarto.OCUPADO, quarto.getSituacao());
        assertTrue(quarto.isOcupado());

        quarto.decrementarOcupacao();
        assertEquals(1, quarto.getOcupacaoAtual());
        assertEquals(StatusQuarto.DISPONIVEL, quarto.getSituacao());

        QuartoDTO dto = new QuartoDTO(quarto);
        assertEquals(1, dto.getVagasDisponiveis());
        Quarto deDto = dto.paraEntidade();
        assertEquals(quarto.getNumeroIdentificacao(), deDto.getNumeroIdentificacao());
    }

    @Test
    @DisplayName("Deve gerenciar status de Consulta")
    public void testConsultaDominio() {
        Paciente paciente = new Paciente(1L, "Lucas", "000.111.222-33", LocalDate.of(2000, 1, 1), "31999990000", "Rua X", "lucas@email.com");
        ProfissionalSaude profissional = new ProfissionalSaude(1L, "Dr. Roberto", "CRM-MG 123", "Clinica Geral", "31988880000", "roberto@email.com");
        LocalDateTime dataHora = LocalDateTime.now().plusDays(2);

        Consulta consulta = new Consulta(paciente, profissional, dataHora, "Consulta de rotina");

        assertTrue(consulta.isAgendada());
        assertEquals(dataHora.toLocalDate(), consulta.getData());
        assertEquals(dataHora.toLocalTime(), consulta.getHorario());

        consulta.finalizar("Paciente liberado sem queixas.");
        assertTrue(consulta.isRealizada());
        assertEquals("Paciente liberado sem queixas.", consulta.getObservacoesMedicas());

        Consulta consulta2 = new Consulta(paciente, profissional, dataHora, "Outra consulta");
        consulta2.cancelar();
        assertTrue(consulta2.isCancelada());
    }

    @Test
    @DisplayName("Deve gerenciar internacao e calcular alta")
    public void testInternacaoDominio() {
        Paciente paciente = new Paciente(1L, "Lucas", "000.111.222-33", LocalDate.of(2000, 1, 1), "31999990000", "Rua X", "lucas@email.com");
        ProfissionalSaude profissional = new ProfissionalSaude(1L, "Dr. Roberto", "CRM-MG 123", "Clinica Geral", "31988880000", "roberto@email.com");
        Quarto quarto = new Quarto(1L, "101", 1, 2, 0, StatusQuarto.DISPONIVEL);

        LocalDateTime entrada = LocalDateTime.now().minusDays(3);
        Internacao internacao = new Internacao(paciente, profissional, quarto, entrada, LocalDate.now(), "Internacao clinica");

        assertTrue(internacao.isEmAndamento());
        assertFalse(internacao.isAlta());
        assertTrue(internacao.calcularDiasInternacao() >= 3);

        LocalDateTime alta = LocalDateTime.now();
        internacao.darAlta(alta, "Paciente recuperado.");

        assertTrue(internacao.isAlta());
        assertFalse(internacao.isEmAndamento());
        assertEquals(alta, internacao.getDataEfetivaAlta());
        assertTrue(internacao.getObservacoes().contains("Paciente recuperado."));
    }

    @Test
    @DisplayName("Deve consolidar historico medico do paciente")
    public void testHistoricoMedicoDominio() {
        Paciente paciente = new Paciente(1L, "Maria", "222.333.444-55", LocalDate.of(1985, 3, 10), "31987654321", "Av Principal", "maria@email.com");
        ProfissionalSaude profissional = new ProfissionalSaude(1L, "Dr. João", "CRM-MG 456", "Ortopedia", "31988881111", "joao@email.com");
        Quarto quarto = new Quarto(1L, "102", 1, 1, 1, StatusQuarto.OCUPADO);

        Consulta consulta1 = new Consulta(1L, paciente, profissional, LocalDateTime.now().minusDays(10), "Dor no joelho", "Prescrito anti-inflamatório", StatusConsulta.REALIZADA);
        Consulta consulta2 = new Consulta(2L, paciente, profissional, LocalDateTime.now().plusDays(5), "Retorno", null, StatusConsulta.AGENDADA);

        Internacao internacao1 = new Internacao(1L, paciente, profissional, quarto, LocalDateTime.now().minusDays(20), LocalDate.now().minusDays(15), LocalDateTime.now().minusDays(15), "Cirurgia no joelho", StatusInternacao.ALTA);
        Internacao internacao2 = new Internacao(2L, paciente, profissional, quarto, LocalDateTime.now().minusDays(1), LocalDate.now().plusDays(2), null, "Observacao pós-queda", StatusInternacao.EM_ANDAMENTO);

        HistoricoMedico historico = new HistoricoMedico(paciente);
        historico.adicionarConsulta(consulta1);
        historico.adicionarConsulta(consulta2);
        historico.adicionarInternacao(internacao1);
        historico.adicionarInternacao(internacao2);
        historico.setInformacoesRelevantes("Paciente com alergia a dipirona");

        assertEquals(2, historico.getTotalConsultas());
        assertEquals(2, historico.getTotalInternacoes());
        assertTrue(historico.possuiInternacaoAtiva());
        assertEquals(1, historico.getConsultasRealizadas().size());
        assertEquals(1, historico.getInternacoesComAlta().size());

        HistoricoMedicoDTO dto = new HistoricoMedicoDTO(historico);
        assertEquals("Maria", dto.getPaciente().getNome());
        assertEquals(2, dto.getTotalConsultas());
        assertEquals(2, dto.getTotalInternacoes());
        assertTrue(dto.isPossuiInternacaoAtiva());
        assertEquals("Paciente com alergia a dipirona", dto.getInformacoesRelevantes());
    }
}
