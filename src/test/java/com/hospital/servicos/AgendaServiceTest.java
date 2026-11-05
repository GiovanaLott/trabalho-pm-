package com.hospital.servicos;

import com.hospital.entidades.Cirurgiao;
import com.hospital.entidades.ConsultaRotina;
import com.hospital.entidades.Paciente;
import com.hospital.entidades.ProcedimentoCirurgico;
import com.hospital.entidades.SituacaoConsulta;
import com.hospital.entidades.StatusCirurgia;
import com.hospital.excecoes.ChoqueHorarioException;
import com.hospital.repositorios.ConsultaRepository;
import com.hospital.repositorios.ProcedimentoCirurgicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private ProcedimentoCirurgicoRepository procedimentoRepository;

    private AgendaService agendaService;
    private Cirurgiao profissional;
    private Paciente paciente;
    private LocalDateTime inicio;

    @BeforeEach
    void setUp() {
        agendaService = new AgendaService(consultaRepository, procedimentoRepository);
        profissional = new Cirurgiao(7L, "Dra. Ana", "CRM700", "Cirurgia Geral", "31990000000", "ana@hospital.com");
        paciente = new Paciente(1L, "Paciente", "123.456.789-09", LocalDate.of(1990, 1, 1),
                "31991111111", "Rua A", "paciente@email.com");
        inicio = LocalDateTime.of(2030, 5, 10, 10, 0);
    }

    @Test
    void deveDetectarSobreposicaoComConsulta() {
        ConsultaRotina consulta = new ConsultaRotina(10L, paciente, profissional, inicio,
                "Retorno", null, SituacaoConsulta.AGENDADA);
        consulta.setDuracaoMinutos(60);
        when(consultaRepository.findByProfissionalId(7L)).thenReturn(List.of(consulta));
        when(procedimentoRepository.findByCirurgiaoId(7L)).thenReturn(List.of());

        assertThrows(ChoqueHorarioException.class,
                () -> agendaService.validarDisponibilidade(7L, inicio.plusMinutes(30),
                        inicio.plusMinutes(90), null, null));
    }

    @Test
    void deveDetectarConflitoCruzadoComCirurgia() {
        ProcedimentoCirurgico cirurgia = cirurgia(inicio, 120, StatusCirurgia.AGENDADA);
        when(consultaRepository.findByProfissionalId(7L)).thenReturn(List.of());
        when(procedimentoRepository.findByCirurgiaoId(7L)).thenReturn(List.of(cirurgia));

        assertThrows(ChoqueHorarioException.class,
                () -> agendaService.validarDisponibilidade(7L, inicio.plusMinutes(90),
                        inicio.plusMinutes(150), null, null));
    }

    @Test
    void devePermitirIntervalosAdjacentesECancelados() {
        ConsultaRotina cancelada = new ConsultaRotina(10L, paciente, profissional, inicio,
                "Retorno", null, SituacaoConsulta.CANCELADA);
        cancelada.setDuracaoMinutos(60);
        ProcedimentoCirurgico adjacente = cirurgia(inicio.plusHours(1), 60, StatusCirurgia.AGENDADA);
        when(consultaRepository.findByProfissionalId(7L)).thenReturn(List.of(cancelada));
        when(procedimentoRepository.findByCirurgiaoId(7L)).thenReturn(List.of(adjacente));

        assertDoesNotThrow(() -> agendaService.validarDisponibilidade(7L, inicio,
                inicio.plusHours(1), null, null));
    }

    @Test
    void deveIgnorarOProprioAtendimentoDuranteAtualizacao() {
        ConsultaRotina consulta = new ConsultaRotina(10L, paciente, profissional, inicio,
                "Retorno", null, SituacaoConsulta.AGENDADA);
        consulta.setDuracaoMinutos(60);
        when(consultaRepository.findByProfissionalId(7L)).thenReturn(List.of(consulta));
        when(procedimentoRepository.findByCirurgiaoId(7L)).thenReturn(List.of());

        assertDoesNotThrow(() -> agendaService.validarDisponibilidade(7L, inicio,
                inicio.plusHours(1), 10L, null));
    }

    private ProcedimentoCirurgico cirurgia(LocalDateTime dataHora, int duracao, StatusCirurgia status) {
        ProcedimentoCirurgico cirurgia = new ProcedimentoCirurgico(20L, "Procedimento", paciente, profissional,
                dataHora.toLocalDate(), dataHora.toLocalTime(), LocalTime.NOON, null, null, status);
        cirurgia.setTipo("Cirurgia geral");
        cirurgia.setDuracaoMinutos(duracao);
        return cirurgia;
    }
}
