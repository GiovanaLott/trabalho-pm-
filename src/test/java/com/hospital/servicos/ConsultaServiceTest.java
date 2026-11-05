package com.hospital.servicos;

import com.hospital.dtos.ConsultaDTO;
import com.hospital.dtos.ConsultaRequestDTO;
import com.hospital.entidades.*;
import com.hospital.excecoes.ChoqueHorarioException;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.ConsultaRepository;
import com.hospital.repositorios.PacienteRepository;
import com.hospital.repositorios.ProfissionalSaudeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ProfissionalSaudeRepository profissionalRepository;

    @InjectMocks
    private ConsultaService consultaService;

    private Paciente paciente;
    private ProfissionalSaude profissional;
    private LocalDateTime dataHoraFutura;

    @BeforeEach
    void setUp() {
        paciente = new Paciente(1L, "João Silva", "123.456.789-00", LocalDate.of(1990, 5, 20), "31999998888", "Rua A", "joao@email.com");
        profissional = new Especialista(1L, "Dra. Maria", "CRM12345", "Cardiologia", "31988887777", "maria@hospital.com");
        dataHoraFutura = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);
    }

    @Test
    @DisplayName("Deve agendar consulta de rotina com sucesso")
    void agendarConsulta_ComSucesso() {
        ConsultaRequestDTO dto = new ConsultaRequestDTO(1L, 1L, dataHoraFutura, "Rotina", "Sem queixas");

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(consultaRepository.findConflitoHorarioProfissional(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        ConsultaRotina consultaSalva = new ConsultaRotina(10L, paciente, profissional, dataHoraFutura, "Rotina", "Sem queixas", SituacaoConsulta.AGENDADA);
        when(consultaRepository.save(any(Consulta.class))).thenReturn(consultaSalva);

        ConsultaDTO resultado = consultaService.agendarConsulta(dto);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals(SituacaoConsulta.AGENDADA, resultado.getSituacao());
        assertEquals(ModalidadeAtendimento.ROTINA, resultado.getModalidade());
        verify(consultaRepository, times(1)).save(any(Consulta.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao agendar consulta em horário com conflito")
    void agendarConsulta_DeveLancarExcecao_QuandoHouverChoqueDeHorario() {
        ConsultaRequestDTO dto = new ConsultaRequestDTO(1L, 1L, dataHoraFutura, "Rotina", null);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));

        Consulta conflito = new ConsultaRotina(5L, paciente, profissional, dataHoraFutura, "Emergência", null, SituacaoConsulta.AGENDADA);
        when(consultaRepository.findConflitoHorarioProfissional(anyLong(), any(), any()))
                .thenReturn(List.of(conflito));

        assertThrows(ChoqueHorarioException.class, () -> consultaService.agendarConsulta(dto));
        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando paciente não for encontrado")
    void agendarConsulta_DeveLancarExcecao_QuandoPacienteNaoEncontrado() {
        ConsultaRequestDTO dto = new ConsultaRequestDTO(99L, 1L, dataHoraFutura, "Rotina", null);
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> consultaService.agendarConsulta(dto));
    }

    @Test
    @DisplayName("Deve agendar consulta especializada quando médico possuir especialidade compatível")
    void agendarConsultaEspecializada_ComSucesso_QuandoEspecialidadeCompativel() {
        ConsultaRequestDTO dto = new ConsultaRequestDTO(1L, 1L, dataHoraFutura, "Avaliação cardíaca", null,
                ModalidadeAtendimento.ESPECIALIZADA, "Cardiologia", null);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(consultaRepository.findConflitoHorarioProfissional(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        ConsultaEspecializada consultaSalva = new ConsultaEspecializada(20L, paciente, profissional, dataHoraFutura,
                "Avaliação cardíaca", null, SituacaoConsulta.AGENDADA, "Cardiologia");
        when(consultaRepository.save(any(Consulta.class))).thenReturn(consultaSalva);

        ConsultaDTO resultado = consultaService.agendarConsulta(dto);

        assertNotNull(resultado);
        assertEquals(ModalidadeAtendimento.ESPECIALIZADA, resultado.getModalidade());
        assertEquals("Cardiologia", resultado.getEspecialidade());
    }

    @Test
    @DisplayName("Deve lançar exceção ao agendar especializada com médico de especialidade diferente")
    void agendarConsultaEspecializada_DeveLancarExcecao_QuandoEspecialidadeIncompativel() {
        ConsultaRequestDTO dto = new ConsultaRequestDTO(1L, 1L, dataHoraFutura, "Dor no joelho", null,
                ModalidadeAtendimento.ESPECIALIZADA, "Ortopedia", null);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(consultaRepository.findConflitoHorarioProfissional(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> consultaService.agendarConsulta(dto));

        assertTrue(ex.getMessage().contains("não possui especialidade compatível"));
        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    @Test
    @DisplayName("Deve agendar atendimento de emergência com classificação de prioridade")
    void agendarAtendimentoEmergencia_ComSucesso() {
        ConsultaRequestDTO dto = new ConsultaRequestDTO(1L, 1L, LocalDateTime.now(), "Suspeita de infarto", null,
                ModalidadeAtendimento.EMERGENCIA, null, PrioridadeAtendimento.CRITICA);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(consultaRepository.findConflitoHorarioProfissional(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        AtendimentoEmergencia salvo = new AtendimentoEmergencia(30L, paciente, profissional, LocalDateTime.now(),
                "Suspeita de infarto", null, SituacaoConsulta.AGENDADA, PrioridadeAtendimento.CRITICA);
        when(consultaRepository.save(any(Consulta.class))).thenReturn(salvo);

        ConsultaDTO resultado = consultaService.agendarConsulta(dto);

        assertNotNull(resultado);
        assertEquals(ModalidadeAtendimento.EMERGENCIA, resultado.getModalidade());
        assertEquals(PrioridadeAtendimento.CRITICA, resultado.getPrioridade());
        assertTrue(resultado.getAguardandoAvaliacao());
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar emergência sem prioridade")
    void agendarAtendimentoEmergencia_DeveLancarExcecao_QuandoSemPrioridade() {
        ConsultaRequestDTO dto = new ConsultaRequestDTO(1L, 1L, LocalDateTime.now(), "Dor aguda", null,
                ModalidadeAtendimento.EMERGENCIA, null, null);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(consultaRepository.findConflitoHorarioProfissional(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        assertThrows(RegraNegocioException.class, () -> consultaService.agendarConsulta(dto));
    }

    @Test
    @DisplayName("Deve executar o ciclo de vida da consulta respeitando as transições de situação")
    void cicloDeVidaConsulta_ComSucesso() {
        ConsultaRotina consulta = new ConsultaRotina(10L, paciente, profissional, dataHoraFutura, "Rotina", null, SituacaoConsulta.AGENDADA);

        when(consultaRepository.findById(10L)).thenReturn(Optional.of(consulta));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 1. Agendada -> Confirmada
        ConsultaDTO confirmada = consultaService.confirmarConsulta(10L);
        assertEquals(SituacaoConsulta.CONFIRMADA, confirmada.getSituacao());

        // 2. Confirmada -> Em Atendimento
        ConsultaDTO emAtendimento = consultaService.iniciarAtendimento(10L);
        assertEquals(SituacaoConsulta.EM_ATENDIMENTO, emAtendimento.getSituacao());

        // 3. Em Atendimento -> Concluída
        ConsultaDTO concluida = consultaService.finalizarConsulta(10L, "Paciente orientado.");
        assertEquals(SituacaoConsulta.CONCLUIDA, concluida.getSituacao());
        assertEquals("Paciente orientado.", concluida.getObservacoesMedicas());
    }

    @Test
    @DisplayName("Deve impedir conclusão direta de consulta que não foi iniciada (ainda agendada)")
    void transicoesDeSituacao_DeveLancarExcecao_AoConcluirConsultaNaoIniciada() {
        ConsultaRotina consulta = new ConsultaRotina(10L, paciente, profissional, dataHoraFutura, "Rotina", null, SituacaoConsulta.AGENDADA);
        when(consultaRepository.findById(10L)).thenReturn(Optional.of(consulta));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> consultaService.finalizarConsulta(10L, "Fim"));
        assertTrue(ex.getMessage().contains("só pode ser concluída se estiver em atendimento"));
    }

    @Test
    @DisplayName("Deve impedir alteração de consulta já concluída")
    void transicoesDeSituacao_DeveLancarExcecao_AoAlterarConsultaJaConcluida() {
        ConsultaRotina consulta = new ConsultaRotina(10L, paciente, profissional, dataHoraFutura, "Rotina", "Observações", SituacaoConsulta.CONCLUIDA);
        when(consultaRepository.findById(10L)).thenReturn(Optional.of(consulta));

        assertThrows(RegraNegocioException.class, () -> consultaService.cancelarConsulta(10L));
        assertThrows(RegraNegocioException.class, () -> consultaService.confirmarConsulta(10L));
    }

    @Test
    @DisplayName("Deve impedir conclusão de consulta que foi cancelada")
    void transicoesDeSituacao_DeveLancarExcecao_AoConcluirConsultaCancelada() {
        ConsultaRotina consulta = new ConsultaRotina(10L, paciente, profissional, dataHoraFutura, "Rotina", null, SituacaoConsulta.CANCELADA);
        when(consultaRepository.findById(10L)).thenReturn(Optional.of(consulta));

        assertThrows(RegraNegocioException.class, () -> consultaService.finalizarConsulta(10L, "Obs"));
    }

    @Test
    @DisplayName("Deve registrar não comparecimento com sucesso")
    void registrarNaoComparecimento_ComSucesso() {
        ConsultaRotina consulta = new ConsultaRotina(10L, paciente, profissional, dataHoraFutura, "Rotina", null, SituacaoConsulta.AGENDADA);
        when(consultaRepository.findById(10L)).thenReturn(Optional.of(consulta));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaDTO resultado = consultaService.registrarNaoComparecimento(10L);
        assertEquals(SituacaoConsulta.NAO_COMPARECEU, resultado.getSituacao());
    }
}
