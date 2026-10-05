package com.hospital.servicos;

import com.hospital.dtos.InternacaoDTO;
import com.hospital.dtos.InternacaoRequestDTO;
import com.hospital.entidades.*;
import com.hospital.excecoes.QuartoLotadoException;
import com.hospital.repositorios.InternacaoRepository;
import com.hospital.repositorios.PacienteRepository;
import com.hospital.repositorios.ProfissionalSaudeRepository;
import com.hospital.repositorios.QuartoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternacaoServiceTest {

    @Mock
    private InternacaoRepository internacaoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ProfissionalSaudeRepository profissionalRepository;

    @Mock
    private QuartoRepository quartoRepository;

    @InjectMocks
    private InternacaoService internacaoService;

    private Paciente paciente;
    private ProfissionalSaude profissional;
    private Quarto quarto;

    @BeforeEach
    void setUp() {
        paciente = new Paciente(1L, "João Silva", "123.456.789-00", LocalDate.of(1990, 5, 20), "31999998888", "Rua A", "joao@email.com");
        profissional = new ProfissionalSaude(1L, "Dra. Maria", "CRM12345", "Cardiologia", "31988887777", "maria@hospital.com");
        quarto = new Quarto(1L, "101-A", 1, 2, 0, StatusQuarto.DISPONIVEL);
    }

    @Test
    void realizarInternacao_ComSucesso() {
        InternacaoRequestDTO dto = new InternacaoRequestDTO(1L, 1L, 1L, LocalDateTime.now(), LocalDate.now().plusDays(5), "Pós-operatório");

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(internacaoRepository.existsByPacienteIdAndStatus(1L, StatusInternacao.EM_ANDAMENTO)).thenReturn(false);

        Internacao internacaoSalva = new Internacao(10L, paciente, profissional, quarto, dto.getDataEntrada(), dto.getDataPrevistaAlta(), null, "Pós-operatório", StatusInternacao.EM_ANDAMENTO);
        when(internacaoRepository.save(any(Internacao.class))).thenReturn(internacaoSalva);

        InternacaoDTO resultado = internacaoService.realizarInternacao(dto);

        assertNotNull(resultado);
        assertEquals(StatusInternacao.EM_ANDAMENTO, resultado.getStatus());
        assertEquals(1, quarto.getOcupacaoAtual());
        verify(quartoRepository, times(1)).save(quarto);
        verify(internacaoRepository, times(1)).save(any(Internacao.class));
    }

    @Test
    void realizarInternacao_DeveLancarExcecao_QuandoQuartoEstiverLotado() {
        quarto.setOcupacaoAtual(2);

        InternacaoRequestDTO dto = new InternacaoRequestDTO(1L, 1L, 1L, LocalDateTime.now(), LocalDate.now().plusDays(5), "Pós-operatório");

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(internacaoRepository.existsByPacienteIdAndStatus(1L, StatusInternacao.EM_ANDAMENTO)).thenReturn(false);

        assertThrows(QuartoLotadoException.class, () -> internacaoService.realizarInternacao(dto));
        verify(internacaoRepository, never()).save(any(Internacao.class));
    }

    @Test
    void darAlta_ComSucesso() {
        quarto.setOcupacaoAtual(1);
        Internacao internacao = new Internacao(10L, paciente, profissional, quarto, LocalDateTime.now().minusDays(3), LocalDate.now(), null, "Observações", StatusInternacao.EM_ANDAMENTO);

        when(internacaoRepository.findById(10L)).thenReturn(Optional.of(internacao));
        when(internacaoRepository.save(any(Internacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InternacaoDTO resultado = internacaoService.darAlta(10L);

        assertNotNull(resultado);
        assertEquals(StatusInternacao.ALTA, resultado.getStatus());
        assertNotNull(resultado.getDataEfetivaAlta());
        assertEquals(0, quarto.getOcupacaoAtual());
        verify(quartoRepository, times(1)).save(quarto);
    }
}
