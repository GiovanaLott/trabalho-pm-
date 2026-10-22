package com.hospital.servicos;

import com.hospital.dtos.ConclusaoCirurgiaDTO;
import com.hospital.dtos.ProcedimentoCirurgicoDTO;
import com.hospital.dtos.ProcedimentoCirurgicoRequestDTO;
import com.hospital.entidades.Paciente;
import com.hospital.entidades.ProcedimentoCirurgico;
import com.hospital.entidades.ProfissionalSaude;
import com.hospital.entidades.StatusCirurgia;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.PacienteRepository;
import com.hospital.repositorios.ProcedimentoCirurgicoRepository;
import com.hospital.repositorios.ProfissionalSaudeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcedimentoCirurgicoServiceTest {

    @Mock
    private ProcedimentoCirurgicoRepository procedimentoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ProfissionalSaudeRepository profissionalRepository;

    @InjectMocks
    private ProcedimentoCirurgicoService procedimentoService;

    private Paciente paciente;
    private ProfissionalSaude cirurgiao;
    private ProcedimentoCirurgico procedimentoAgendado;

    @BeforeEach
    void setUp() {
        paciente = new Paciente(1L, "Carlos Alberto", "111.222.333-44", LocalDate.of(1985, 3, 15), "3199887766", "Rua X", "carlos@email.com");
        cirurgiao = new ProfissionalSaude(2L, "Dr. Roberto", "CRM99887", "Cirurgião Geral", "3198776655", "roberto@hospital.com");

        procedimentoAgendado = new ProcedimentoCirurgico(
                10L,
                "Apendicectomia",
                paciente,
                cirurgiao,
                LocalDate.now().plusDays(3),
                LocalTime.of(8, 0),
                null,
                "Jejum de 8h",
                null,
                StatusCirurgia.AGENDADA
        );
    }

    @Test
    @DisplayName("Deve agendar procedimento cirúrgico com sucesso")
    void agendar_ComSucesso() {
        ProcedimentoCirurgicoRequestDTO request = new ProcedimentoCirurgicoRequestDTO(
                "Apendicectomia", 1L, 2L, LocalDate.now().plusDays(3), LocalTime.of(8, 0), "Jejum de 8h"
        );

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(2L)).thenReturn(Optional.of(cirurgiao));
        when(procedimentoRepository.save(any(ProcedimentoCirurgico.class))).thenReturn(procedimentoAgendado);

        ProcedimentoCirurgicoDTO resultado = procedimentoService.agendar(request);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals("Apendicectomia", resultado.getDescricao());
        assertEquals(StatusCirurgia.AGENDADA, resultado.getSituacao());
        verify(procedimentoRepository, times(1)).save(any(ProcedimentoCirurgico.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao agendar com paciente inexistente")
    void agendar_DeveLancarExcecao_QuandoPacienteNaoEncontrado() {
        ProcedimentoCirurgicoRequestDTO request = new ProcedimentoCirurgicoRequestDTO(
                "Apendicectomia", 99L, 2L, LocalDate.now().plusDays(3), LocalTime.of(8, 0), "Jejum"
        );

        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> procedimentoService.agendar(request));
        verify(procedimentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao agendar com cirurgião inexistente")
    void agendar_DeveLancarExcecao_QuandoCirurgiaoNaoEncontrado() {
        ProcedimentoCirurgicoRequestDTO request = new ProcedimentoCirurgicoRequestDTO(
                "Apendicectomia", 1L, 99L, LocalDate.now().plusDays(3), LocalTime.of(8, 0), "Jejum"
        );

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> procedimentoService.agendar(request));
        verify(procedimentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve iniciar cirurgia agendada com sucesso")
    void iniciarCirurgia_ComSucesso() {
        when(procedimentoRepository.findById(10L)).thenReturn(Optional.of(procedimentoAgendado));
        when(procedimentoRepository.save(any(ProcedimentoCirurgico.class))).thenAnswer(i -> i.getArgument(0));

        ProcedimentoCirurgicoDTO resultado = procedimentoService.iniciarCirurgia(10L);

        assertNotNull(resultado);
        assertEquals(StatusCirurgia.EM_ANDAMENTO, resultado.getSituacao());
        verify(procedimentoRepository, times(1)).save(procedimentoAgendado);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar iniciar cirurgia já cancelada")
    void iniciarCirurgia_DeveLancarExcecao_QuandoCirurgiaCancelada() {
        procedimentoAgendado.setSituacao(StatusCirurgia.CANCELADA);
        when(procedimentoRepository.findById(10L)).thenReturn(Optional.of(procedimentoAgendado));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> procedimentoService.iniciarCirurgia(10L));
        assertTrue(ex.getMessage().contains("Não é possível iniciar uma cirurgia cancelada"));
        verify(procedimentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar iniciar cirurgia já concluída")
    void iniciarCirurgia_DeveLancarExcecao_QuandoCirurgiaJaConcluida() {
        procedimentoAgendado.setSituacao(StatusCirurgia.CONCLUIDA);
        when(procedimentoRepository.findById(10L)).thenReturn(Optional.of(procedimentoAgendado));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> procedimentoService.iniciarCirurgia(10L));
        assertTrue(ex.getMessage().contains("Não é possível iniciar uma cirurgia que já foi concluída"));
        verify(procedimentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("REGRA OBRIGATÓRIA: Não pode concluir cirurgia sem que ela tenha sido iniciada")
    void concluirCirurgia_DeveLancarExcecao_QuandoCirurgiaNaoFoiIniciada() {
        // Situação atual é AGENDADA (não iniciada)
        when(procedimentoRepository.findById(10L)).thenReturn(Optional.of(procedimentoAgendado));

        ConclusaoCirurgiaDTO conclusao = new ConclusaoCirurgiaDTO(LocalTime.of(10, 30), "Sucesso");

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> procedimentoService.concluirCirurgia(10L, conclusao));

        assertEquals("Uma cirurgia não pode ser concluída se ainda não tiver sido iniciada.", ex.getMessage());
        verify(procedimentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve concluir cirurgia que está em andamento")
    void concluirCirurgia_ComSucesso_QuandoIniciada() {
        procedimentoAgendado.setSituacao(StatusCirurgia.EM_ANDAMENTO);
        when(procedimentoRepository.findById(10L)).thenReturn(Optional.of(procedimentoAgendado));
        when(procedimentoRepository.save(any(ProcedimentoCirurgico.class))).thenAnswer(i -> i.getArgument(0));

        ConclusaoCirurgiaDTO conclusao = new ConclusaoCirurgiaDTO(LocalTime.of(10, 30), "Procedimento realizado sem intercorrências.");

        ProcedimentoCirurgicoDTO resultado = procedimentoService.concluirCirurgia(10L, conclusao);

        assertNotNull(resultado);
        assertEquals(StatusCirurgia.CONCLUIDA, resultado.getSituacao());
        assertEquals(LocalTime.of(10, 30), resultado.getHorarioTermino());
        assertEquals("Procedimento realizado sem intercorrências.", resultado.getObservacoesPosOperatorias());
        verify(procedimentoRepository, times(1)).save(procedimentoAgendado);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar concluir cirurgia já concluída")
    void concluirCirurgia_DeveLancarExcecao_QuandoJaConcluida() {
        procedimentoAgendado.setSituacao(StatusCirurgia.CONCLUIDA);
        when(procedimentoRepository.findById(10L)).thenReturn(Optional.of(procedimentoAgendado));

        ConclusaoCirurgiaDTO conclusao = new ConclusaoCirurgiaDTO(LocalTime.of(11, 0), "Obs");

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> procedimentoService.concluirCirurgia(10L, conclusao));

        assertTrue(ex.getMessage().contains("já foi concluída anteriormente"));
        verify(procedimentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve cancelar cirurgia agendada com sucesso")
    void cancelarCirurgia_ComSucesso() {
        when(procedimentoRepository.findById(10L)).thenReturn(Optional.of(procedimentoAgendado));
        when(procedimentoRepository.save(any(ProcedimentoCirurgico.class))).thenAnswer(i -> i.getArgument(0));

        ProcedimentoCirurgicoDTO resultado = procedimentoService.cancelarCirurgia(10L);

        assertNotNull(resultado);
        assertEquals(StatusCirurgia.CANCELADA, resultado.getSituacao());
        verify(procedimentoRepository, times(1)).save(procedimentoAgendado);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cancelar cirurgia que já foi concluída")
    void cancelarCirurgia_DeveLancarExcecao_QuandoJaConcluida() {
        procedimentoAgendado.setSituacao(StatusCirurgia.CONCLUIDA);
        when(procedimentoRepository.findById(10L)).thenReturn(Optional.of(procedimentoAgendado));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> procedimentoService.cancelarCirurgia(10L));
        assertTrue(ex.getMessage().contains("Não é possível cancelar uma cirurgia que já foi concluída"));
        verify(procedimentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cancelar cirurgia já cancelada")
    void cancelarCirurgia_DeveLancarExcecao_QuandoJaCancelada() {
        procedimentoAgendado.setSituacao(StatusCirurgia.CANCELADA);
        when(procedimentoRepository.findById(10L)).thenReturn(Optional.of(procedimentoAgendado));

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> procedimentoService.cancelarCirurgia(10L));
        assertTrue(ex.getMessage().contains("Esta cirurgia já está cancelada"));
        verify(procedimentoRepository, never()).save(any());
    }
}
