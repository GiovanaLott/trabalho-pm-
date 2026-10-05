package com.hospital.servicos;

import com.hospital.dtos.QuartoDTO;
import com.hospital.entidades.Quarto;
import com.hospital.entidades.StatusQuarto;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.QuartoRepository;
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
class QuartoServiceTest {

    @Mock
    private QuartoRepository quartoRepository;

    @InjectMocks
    private QuartoService quartoService;

    private Quarto quarto;
    private QuartoDTO quartoDTO;

    @BeforeEach
    void setUp() {
        quarto = new Quarto(1L, "101-A", 1, 2, 0, StatusQuarto.DISPONIVEL);
        quartoDTO = new QuartoDTO(quarto);
    }

    @Test
    void cadastrarQuarto_ComSucesso() {
        when(quartoRepository.existsByNumeroIdentificacao(quartoDTO.getNumeroIdentificacao())).thenReturn(false);
        when(quartoRepository.save(any(Quarto.class))).thenReturn(quarto);

        QuartoDTO resultado = quartoService.cadastrarQuarto(quartoDTO);

        assertNotNull(resultado);
        assertEquals(quarto.getNumeroIdentificacao(), resultado.getNumeroIdentificacao());
        assertEquals(StatusQuarto.DISPONIVEL, resultado.getSituacao());
        verify(quartoRepository, times(1)).save(any(Quarto.class));
    }

    @Test
    void cadastrarQuarto_DeveLancarExcecao_QuandoIdentificacaoJaExiste() {
        when(quartoRepository.existsByNumeroIdentificacao(quartoDTO.getNumeroIdentificacao())).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> quartoService.cadastrarQuarto(quartoDTO));
        verify(quartoRepository, never()).save(any(Quarto.class));
    }

    @Test
    void listarTodos_ComSucesso() {
        when(quartoRepository.findAll()).thenReturn(List.of(quarto));

        List<QuartoDTO> resultado = quartoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(quarto.getNumeroIdentificacao(), resultado.get(0).getNumeroIdentificacao());
    }

    @Test
    void listarDisponiveis_ComSucesso() {
        when(quartoRepository.findBySituacao(StatusQuarto.DISPONIVEL)).thenReturn(List.of(quarto));

        List<QuartoDTO> resultado = quartoService.listarDisponiveis();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(StatusQuarto.DISPONIVEL, resultado.get(0).getSituacao());
    }

    @Test
    void buscarPorId_ComSucesso() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));

        QuartoDTO resultado = quartoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(quarto.getNumeroIdentificacao(), resultado.getNumeroIdentificacao());
    }

    @Test
    void buscarPorId_DeveLancarExcecao_QuandoNaoEncontrado() {
        when(quartoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> quartoService.buscarPorId(99L));
    }
}
