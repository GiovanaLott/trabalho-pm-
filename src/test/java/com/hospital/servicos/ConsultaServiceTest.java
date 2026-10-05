package com.hospital.servicos;

import com.hospital.dtos.ConsultaDTO;
import com.hospital.dtos.ConsultaRequestDTO;
import com.hospital.entidades.Consulta;
import com.hospital.entidades.Paciente;
import com.hospital.entidades.ProfissionalSaude;
import com.hospital.entidades.StatusConsulta;
import com.hospital.excecoes.ChoqueHorarioException;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.repositorios.ConsultaRepository;
import com.hospital.repositorios.PacienteRepository;
import com.hospital.repositorios.ProfissionalSaudeRepository;
import org.junit.jupiter.api.BeforeEach;
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
        profissional = new ProfissionalSaude(1L, "Dra. Maria", "CRM12345", "Cardiologia", "31988887777", "maria@hospital.com");
        dataHoraFutura = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);
    }

    @Test
    void agendarConsulta_ComSucesso() {
        ConsultaRequestDTO dto = new ConsultaRequestDTO(1L, 1L, dataHoraFutura, "Rotina", "Sem queixas");

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(consultaRepository.findConflitoHorarioProfissional(anyLong(), any(), any(), eq(StatusConsulta.CANCELADA)))
                .thenReturn(Collections.emptyList());

        Consulta consultaSalva = new Consulta(10L, paciente, profissional, dataHoraFutura, "Rotina", "Sem queixas", StatusConsulta.AGENDADA);
        when(consultaRepository.save(any(Consulta.class))).thenReturn(consultaSalva);

        ConsultaDTO resultado = consultaService.agendarConsulta(dto);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals(StatusConsulta.AGENDADA, resultado.getStatus());
        verify(consultaRepository, times(1)).save(any(Consulta.class));
    }

    @Test
    void agendarConsulta_DeveLancarExcecao_QuandoHouverChoqueDeHorario() {
        ConsultaRequestDTO dto = new ConsultaRequestDTO(1L, 1L, dataHoraFutura, "Rotina", null);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));

        Consulta conflito = new Consulta(5L, paciente, profissional, dataHoraFutura, "Emergência", null, StatusConsulta.AGENDADA);
        when(consultaRepository.findConflitoHorarioProfissional(anyLong(), any(), any(), eq(StatusConsulta.CANCELADA)))
                .thenReturn(List.of(conflito));

        assertThrows(ChoqueHorarioException.class, () -> consultaService.agendarConsulta(dto));
        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    @Test
    void agendarConsulta_DeveLancarExcecao_QuandoPacienteNaoEncontrado() {
        ConsultaRequestDTO dto = new ConsultaRequestDTO(99L, 1L, dataHoraFutura, "Rotina", null);
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> consultaService.agendarConsulta(dto));
    }
}
