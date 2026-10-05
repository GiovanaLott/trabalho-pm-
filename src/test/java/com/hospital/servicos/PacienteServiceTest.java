package com.hospital.servicos;

import com.hospital.dtos.PacienteDTO;
import com.hospital.entidades.Paciente;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteService pacienteService;

    private Paciente paciente;
    private PacienteDTO pacienteDTO;

    @BeforeEach
    void setUp() {
        paciente = new Paciente(1L, "João Silva", "123.456.789-00", LocalDate.of(1990, 5, 20), "31999998888", "Rua A, 123", "joao@email.com");
        pacienteDTO = new PacienteDTO(paciente);
    }

    @Test
    void cadastrar_ComSucesso() {
        when(pacienteRepository.existsByCpf(pacienteDTO.getCpf())).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        PacienteDTO resultado = pacienteService.cadastrar(pacienteDTO);

        assertNotNull(resultado);
        assertEquals(paciente.getNome(), resultado.getNome());
        assertEquals(paciente.getCpf(), resultado.getCpf());
        verify(pacienteRepository, times(1)).save(any(Paciente.class));
    }

    @Test
    void cadastrar_DeveLancarExcecao_QuandoCpfJaExiste() {
        when(pacienteRepository.existsByCpf(pacienteDTO.getCpf())).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> pacienteService.cadastrar(pacienteDTO));
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void listarTodos_ComSucesso() {
        when(pacienteRepository.findAll()).thenReturn(List.of(paciente));

        List<PacienteDTO> resultado = pacienteService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(paciente.getNome(), resultado.get(0).getNome());
        verify(pacienteRepository, times(1)).findAll();
    }

    @Test
    void buscarPorId_ComSucesso() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        PacienteDTO resultado = pacienteService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(paciente.getNome(), resultado.getNome());
    }

    @Test
    void buscarPorId_DeveLancarExcecao_QuandoNaoEncontrado() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> pacienteService.buscarPorId(99L));
    }
}
