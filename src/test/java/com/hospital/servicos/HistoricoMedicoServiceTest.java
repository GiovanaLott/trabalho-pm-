package com.hospital.servicos;

import com.hospital.dtos.HistoricoMedicoDTO;
import com.hospital.entidades.*;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.repositorios.ConsultaRepository;
import com.hospital.repositorios.InternacaoRepository;
import com.hospital.repositorios.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoricoMedicoServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private InternacaoRepository internacaoRepository;

    @InjectMocks
    private HistoricoMedicoService historicoMedicoService;

    private Paciente paciente;
    private Consulta consulta;
    private Internacao internacao;

    @BeforeEach
    void setUp() {
        paciente = new Paciente(1L, "João Silva", "123.456.789-00", LocalDate.of(1990, 5, 20), "31999998888", "Rua A", "joao@email.com");
        ProfissionalSaude profissional = new ProfissionalSaude(1L, "Dra. Maria", "CRM12345", "Cardiologia", "31988887777", "maria@hospital.com");
        Quarto quarto = new Quarto(1L, "101-A", 1, 2, 1, StatusQuarto.DISPONIVEL);

        consulta = new Consulta(10L, paciente, profissional, LocalDateTime.now().minusDays(10), "Check-up", "Sem observações", StatusConsulta.REALIZADA);
        internacao = new Internacao(20L, paciente, profissional, quarto, LocalDateTime.now().minusDays(5), LocalDate.now(), null, "Observações gerais", StatusInternacao.EM_ANDAMENTO);
    }

    @Test
    void buscarHistoricoPorPaciente_ComSucesso() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(consultaRepository.findByPacienteId(1L)).thenReturn(List.of(consulta));
        when(internacaoRepository.findByPacienteId(1L)).thenReturn(List.of(internacao));

        HistoricoMedicoDTO historico = historicoMedicoService.buscarHistoricoPorPaciente(1L);

        assertNotNull(historico);
        assertEquals(paciente.getNome(), historico.getPaciente().getNome());
        assertEquals(1, historico.getConsultas().size());
        assertEquals(1, historico.getInternacoes().size());

        verify(pacienteRepository, times(1)).findById(1L);
        verify(consultaRepository, times(1)).findByPacienteId(1L);
        verify(internacaoRepository, times(1)).findByPacienteId(1L);
    }

    @Test
    void buscarHistoricoPorPaciente_DeveLancarExcecao_QuandoPacienteNaoEncontrado() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> historicoMedicoService.buscarHistoricoPorPaciente(99L));
        verify(consultaRepository, never()).findByPacienteId(anyLong());
        verify(internacaoRepository, never()).findByPacienteId(anyLong());
    }
}
