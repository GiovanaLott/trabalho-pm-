package com.hospital.servicos;

import com.hospital.dtos.PacienteDTO;
import com.hospital.entidades.Paciente;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.ConsultaRepository;
import com.hospital.repositorios.InternacaoRepository;
import com.hospital.repositorios.PacienteRepository;
import com.hospital.repositorios.ProcedimentoCirurgicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    private static final String CPF = "529.982.247-25";

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private InternacaoRepository internacaoRepository;

    @Mock
    private ProcedimentoCirurgicoRepository procedimentoRepository;

    @InjectMocks
    private PacienteService pacienteService;

    private Paciente paciente;
    private PacienteDTO pacienteDTO;

    @BeforeEach
    void setUp() {
        paciente = new Paciente(1L, "João Silva", CPF, LocalDate.of(1990, 5, 20), "31999998888", "Rua A, 123", "joao@email.com");
        pacienteDTO = new PacienteDTO(null, "João Silva", CPF, LocalDate.of(1990, 5, 20), "31999998888", "Rua A, 123", "joao@email.com");
    }

    @Test
    @DisplayName("Cadastra paciente com CPF válido")
    void cadastrar_ComSucesso() {
        when(pacienteRepository.existsByCpf(CPF)).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        PacienteDTO resultado = pacienteService.cadastrar(pacienteDTO);

        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        assertEquals(CPF, resultado.getCpf());
        assertNotNull(resultado.getIdade());
        verify(pacienteRepository).save(any(Paciente.class));
    }

    @Test
    @DisplayName("Armazena o CPF sempre com máscara, mesmo se enviado só com números")
    void cadastrar_DeveNormalizarCpf() {
        pacienteDTO.setCpf("52998224725");
        pacienteDTO.setNome("  João Silva  ");
        when(pacienteRepository.existsByCpf(CPF)).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        pacienteService.cadastrar(pacienteDTO);

        ArgumentCaptor<Paciente> captor = ArgumentCaptor.forClass(Paciente.class);
        verify(pacienteRepository).save(captor.capture());
        assertEquals(CPF, captor.getValue().getCpf());
        assertEquals("João Silva", captor.getValue().getNome());
    }

    @Test
    @DisplayName("Não cadastra CPF já existente (mesmo digitado sem máscara)")
    void cadastrar_DeveLancarExcecao_QuandoCpfJaExiste() {
        pacienteDTO.setCpf("52998224725");
        when(pacienteRepository.existsByCpf(CPF)).thenReturn(true);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> pacienteService.cadastrar(pacienteDTO));
        assertTrue(ex.getMessage().contains(CPF));
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não cadastra CPF inválido")
    void cadastrar_DeveLancarExcecao_QuandoCpfInvalido() {
        pacienteDTO.setCpf("123.456.789-00");
        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> pacienteService.cadastrar(pacienteDTO));
        assertTrue(ex.getMessage().startsWith("CPF inválido"));
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não cadastra data de nascimento futura ou com idade acima de 130 anos")
    void cadastrar_DeveLancarExcecao_QuandoDataNascimentoInvalida() {
        when(pacienteRepository.existsByCpf(CPF)).thenReturn(false);

        pacienteDTO.setDataNascimento(LocalDate.now().plusDays(1));
        assertThrows(RegraNegocioException.class, () -> pacienteService.cadastrar(pacienteDTO));

        pacienteDTO.setDataNascimento(LocalDate.now().minusYears(131));
        assertThrows(RegraNegocioException.class, () -> pacienteService.cadastrar(pacienteDTO));

        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void listarTodos_ComSucesso() {
        when(pacienteRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(paciente));

        List<PacienteDTO> resultado = pacienteService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNome());
    }

    @Test
    @DisplayName("Busca por nome usa filtro parcial; sem nome, lista todos")
    void buscarPorNome() {
        when(pacienteRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc("joão")).thenReturn(List.of(paciente));
        assertEquals(1, pacienteService.buscarPorNome(" joão ").size());

        when(pacienteRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(paciente));
        assertEquals(1, pacienteService.buscarPorNome("  ").size());
        verify(pacienteRepository).findAllByOrderByNomeAsc();
    }

    @Test
    void buscarPorId_ComSucesso() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        assertEquals(1L, pacienteService.buscarPorId(1L).getId());
    }

    @Test
    void buscarPorId_DeveLancarExcecao_QuandoNaoEncontrado() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> pacienteService.buscarPorId(99L));
    }

    @Test
    @DisplayName("Busca por CPF aceita CPF sem máscara")
    void buscarPorCpf_ComSucesso() {
        when(pacienteRepository.findByCpf(CPF)).thenReturn(Optional.of(paciente));
        assertEquals("João Silva", pacienteService.buscarPorCpf("52998224725").getNome());
    }

    @Test
    void buscarPorCpf_DeveLancarExcecao_QuandoNaoEncontrado() {
        when(pacienteRepository.findByCpf(CPF)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> pacienteService.buscarPorCpf(CPF));
    }

    @Test
    @DisplayName("Atualiza os dados do paciente")
    void atualizar_ComSucesso() {
        PacienteDTO alteracao = new PacienteDTO(null, "João da Silva", CPF, LocalDate.of(1990, 5, 20), "31911112222", "Rua B, 50", "novo@email.com");
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.existsByCpfAndIdNot(CPF, 1L)).thenReturn(false);
        when(pacienteRepository.save(paciente)).thenReturn(paciente);

        PacienteDTO resultado = pacienteService.atualizar(1L, alteracao);

        assertEquals("João da Silva", resultado.getNome());
        assertEquals("Rua B, 50", resultado.getEndereco());
        assertEquals("novo@email.com", resultado.getEmail());
    }

    @Test
    @DisplayName("Não permite atualizar para o CPF de outro paciente")
    void atualizar_DeveLancarExcecao_QuandoCpfPertenceAOutroPaciente() {
        PacienteDTO alteracao = new PacienteDTO(null, "João Silva", "111.444.777-35", LocalDate.of(1990, 5, 20), null, null, null);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.existsByCpfAndIdNot("111.444.777-35", 1L)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> pacienteService.atualizar(1L, alteracao));
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Exclui paciente sem histórico")
    void excluir_ComSucesso() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(consultaRepository.existsByPacienteId(1L)).thenReturn(false);
        when(internacaoRepository.existsByPacienteId(1L)).thenReturn(false);
        when(procedimentoRepository.existsByPacienteId(1L)).thenReturn(false);

        pacienteService.excluir(1L);

        verify(pacienteRepository).delete(paciente);
    }

    @Test
    @DisplayName("Não exclui paciente com consultas, internações ou cirurgias (preserva o histórico)")
    void excluir_DeveLancarExcecao_QuandoPossuiHistorico() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(consultaRepository.existsByPacienteId(1L)).thenReturn(false);
        when(internacaoRepository.existsByPacienteId(1L)).thenReturn(true);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class, () -> pacienteService.excluir(1L));
        assertTrue(ex.getMessage().contains("histórico"));
        verify(pacienteRepository, never()).delete(any());
    }
}
