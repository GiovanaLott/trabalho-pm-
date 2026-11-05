package com.hospital.servicos;

import com.hospital.dtos.ProfissionalSaudeDTO;
import com.hospital.entidades.CategoriaProfissional;
import com.hospital.entidades.Cirurgiao;
import com.hospital.entidades.Especialista;
import com.hospital.entidades.ProfissionalSaude;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.ProfissionalSaudeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfissionalSaudeServiceTest {

    @Mock
    private ProfissionalSaudeRepository profissionalRepository;

    @InjectMocks
    private ProfissionalSaudeService profissionalService;

    private ProfissionalSaude profissional;
    private ProfissionalSaudeDTO profissionalDTO;

    @BeforeEach
    void setUp() {
        profissional = new Especialista(1L, "Dra. Maria Santos", "CRM12345", "Cardiologia", "31988887777", "maria@hospital.com");
        profissionalDTO = new ProfissionalSaudeDTO(profissional);
    }

    @Test
    void cadastrar_ComSucesso() {
        when(profissionalRepository.existsByRegistroProfissional(profissionalDTO.getRegistroProfissional())).thenReturn(false);
        when(profissionalRepository.save(any(ProfissionalSaude.class))).thenReturn(profissional);

        ProfissionalSaudeDTO resultado = profissionalService.cadastrar(profissionalDTO);

        assertNotNull(resultado);
        assertEquals(profissional.getNome(), resultado.getNome());
        assertEquals(profissional.getRegistroProfissional(), resultado.getRegistroProfissional());
        verify(profissionalRepository, times(1)).save(any(ProfissionalSaude.class));
    }

    @Test
    void cadastrar_DeveCriarACategoriaInformada() {
        ProfissionalSaudeDTO dto = new ProfissionalSaudeDTO();
        dto.setNome("Dr. Roberto");
        dto.setRegistroProfissional("CRM-CIR-1");
        dto.setCategoria(CategoriaProfissional.CIRURGIAO);
        dto.setEspecialidade("Cirurgia Geral");

        when(profissionalRepository.existsByRegistroProfissional("CRM-CIR-1")).thenReturn(false);
        when(profissionalRepository.save(any(ProfissionalSaude.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        ProfissionalSaudeDTO resultado = profissionalService.cadastrar(dto);

        assertEquals(CategoriaProfissional.CIRURGIAO, resultado.getCategoria());
        verify(profissionalRepository).save(isA(Cirurgiao.class));
    }

    @Test
    void cadastrar_DeveExigirEspecialidadeSomenteParaEspecialista() {
        ProfissionalSaudeDTO dto = new ProfissionalSaudeDTO();
        dto.setNome("Dra. Ana");
        dto.setRegistroProfissional("CRM-ESP-1");
        dto.setCategoria(CategoriaProfissional.ESPECIALISTA);

        when(profissionalRepository.existsByRegistroProfissional("CRM-ESP-1")).thenReturn(false);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> profissionalService.cadastrar(dto));

        assertTrue(erro.getMessage().contains("especialidade"));
        verify(profissionalRepository, never()).save(any());
    }

    @Test
    void cadastrar_DeveLancarExcecao_QuandoRegistroJaExiste() {
        when(profissionalRepository.existsByRegistroProfissional(profissionalDTO.getRegistroProfissional())).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> profissionalService.cadastrar(profissionalDTO));
        verify(profissionalRepository, never()).save(any(ProfissionalSaude.class));
    }

    @Test
    void listarTodos_ComSucesso() {
        when(profissionalRepository.findAll()).thenReturn(List.of(profissional));

        List<ProfissionalSaudeDTO> resultado = profissionalService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(profissional.getNome(), resultado.get(0).getNome());
        verify(profissionalRepository, times(1)).findAll();
    }

    @Test
    void buscarPorId_ComSucesso() {
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));

        ProfissionalSaudeDTO resultado = profissionalService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(profissional.getNome(), resultado.getNome());
    }

    @Test
    void buscarPorId_DeveLancarExcecao_QuandoNaoEncontrado() {
        when(profissionalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> profissionalService.buscarPorId(99L));
    }
}
