package com.hospital.servicos;

import com.hospital.dtos.OcupacaoHospitalarDTO;
import com.hospital.dtos.QuartoDTO;
import com.hospital.dtos.QuartoUpdateDTO;
import com.hospital.entidades.*;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.repositorios.InternacaoRepository;
import com.hospital.repositorios.QuartoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuartoServiceTest {

    @Mock
    private QuartoRepository quartoRepository;

    @Mock
    private InternacaoRepository internacaoRepository;

    @InjectMocks
    private QuartoService quartoService;

    private Quarto quarto;

    @BeforeEach
    void setUp() {
        quarto = new Enfermaria(1L, "101-A", 1, 2, 0, StatusQuarto.DISPONIVEL);
    }

    private QuartoDTO novoQuarto(String numero, TipoQuarto tipo, int capacidade) {
        QuartoDTO dto = new QuartoDTO();
        dto.setNumeroIdentificacao(numero);
        dto.setAndar(1);
        dto.setCapacidadeMaxima(capacidade);
        dto.setTipo(tipo);
        return dto;
    }

    @Test
    @DisplayName("Cadastra quarto sem tipo como enfermaria, vazio e disponível")
    void cadastrarQuarto_ComSucesso() {
        QuartoDTO dto = novoQuarto("101-A", null, 2);
        dto.setOcupacaoAtual(5);                  // deve ser ignorado
        dto.setSituacao(StatusQuarto.OCUPADO);    // deve ser ignorado
        when(quartoRepository.existsByNumeroIdentificacao("101-A")).thenReturn(false);
        when(quartoRepository.save(any(Quarto.class))).thenAnswer(inv -> inv.getArgument(0));

        QuartoDTO resultado = quartoService.cadastrarQuarto(dto);

        assertEquals(TipoQuarto.ENFERMARIA, resultado.getTipo());
        assertEquals(0, resultado.getOcupacaoAtual());
        assertEquals(StatusQuarto.DISPONIVEL, resultado.getSituacao());
        assertEquals(2, resultado.getLeitosDisponiveis());
    }

    @Test
    @DisplayName("Cadastra cada tipo de quarto com a subclasse e os atributos corretos")
    void cadastrarQuarto_PorTipo() {
        when(quartoRepository.existsByNumeroIdentificacao(any())).thenReturn(false);
        when(quartoRepository.save(any(Quarto.class))).thenAnswer(inv -> inv.getArgument(0));

        QuartoDTO apartamento = novoQuarto("AP-1", TipoQuarto.APARTAMENTO, 1);
        apartamento.setPermiteAcompanhante(false);
        QuartoDTO isolamento = novoQuarto("ISO-1", TipoQuarto.ISOLAMENTO, 1);
        isolamento.setTipoIsolamento(TipoIsolamento.CONTATO);

        assertFalse(quartoService.cadastrarQuarto(apartamento).getPermiteAcompanhante());
        assertEquals(TipoIsolamento.CONTATO, quartoService.cadastrarQuarto(isolamento).getTipoIsolamento());
        assertEquals(TipoQuarto.UTI, quartoService.cadastrarQuarto(novoQuarto("UTI-1", TipoQuarto.UTI, 8)).getTipo());

        ArgumentCaptor<Quarto> captor = ArgumentCaptor.forClass(Quarto.class);
        verify(quartoRepository, times(3)).save(captor.capture());
        assertInstanceOf(Apartamento.class, captor.getAllValues().get(0));
        assertInstanceOf(QuartoIsolamento.class, captor.getAllValues().get(1));
        assertInstanceOf(UnidadeTerapiaIntensiva.class, captor.getAllValues().get(2));
    }

    @Test
    @DisplayName("Rejeita capacidade fora da faixa do tipo")
    void cadastrarQuarto_DeveLancarExcecao_QuandoCapacidadeForaDoTipo() {
        when(quartoRepository.existsByNumeroIdentificacao(any())).thenReturn(false);
        assertThrows(RegraNegocioException.class,
                () -> quartoService.cadastrarQuarto(novoQuarto("AP-1", TipoQuarto.APARTAMENTO, 4)));
        assertThrows(RegraNegocioException.class,
                () -> quartoService.cadastrarQuarto(novoQuarto("ISO-1", TipoQuarto.ISOLAMENTO, 1))); // sem tipo de isolamento
        verify(quartoRepository, never()).save(any());
    }

    @Test
    void cadastrarQuarto_DeveLancarExcecao_QuandoIdentificacaoJaExiste() {
        when(quartoRepository.existsByNumeroIdentificacao("101-A")).thenReturn(true);
        assertThrows(RegraNegocioException.class, () -> quartoService.cadastrarQuarto(novoQuarto("101-A", null, 2)));
        verify(quartoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lista com filtros por tipo, andar e situação")
    void listar_ComFiltros() {
        Quarto uti = new UnidadeTerapiaIntensiva(2L, "UTI-1", 3, 4, 4, StatusQuarto.OCUPADO);
        Quarto enfermaria2 = new Enfermaria(3L, "301", 3, 4, 0, StatusQuarto.DISPONIVEL);
        when(quartoRepository.findAllByOrderByAndarAscNumeroIdentificacaoAsc()).thenReturn(List.of(quarto, uti, enfermaria2));

        assertEquals(3, quartoService.listarTodos().size());
        assertEquals(List.of("UTI-1"), quartoService.listar(TipoQuarto.UTI, null, null).stream().map(QuartoDTO::getNumeroIdentificacao).toList());
        assertEquals(2, quartoService.listar(null, 3, null).size());
        assertEquals(List.of("301"), quartoService.listar(TipoQuarto.ENFERMARIA, 3, StatusQuarto.DISPONIVEL).stream().map(QuartoDTO::getNumeroIdentificacao).toList());
    }

    @Test
    void listarDisponiveis_ComSucesso() {
        Quarto apartamento = new Apartamento(2L, "AP-1", 2, 1, 0, StatusQuarto.DISPONIVEL, true);
        when(quartoRepository.findBySituacao(StatusQuarto.DISPONIVEL)).thenReturn(List.of(quarto, apartamento));

        assertEquals(2, quartoService.listarDisponiveis().size());
        List<QuartoDTO> apenasApartamentos = quartoService.listarDisponiveis(TipoQuarto.APARTAMENTO);
        assertEquals(1, apenasApartamentos.size());
        assertEquals("AP-1", apenasApartamentos.get(0).getNumeroIdentificacao());
    }

    @Test
    void buscarPorId_ComSucesso() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        assertEquals("101-A", quartoService.buscarPorId(1L).getNumeroIdentificacao());
    }

    @Test
    void buscarPorId_DeveLancarExcecao_QuandoNaoEncontrado() {
        when(quartoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> quartoService.buscarPorId(99L));
    }

    @Test
    @DisplayName("Atualiza número, andar e capacidade")
    void atualizar_ComSucesso() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(quartoRepository.existsByNumeroIdentificacaoAndIdNot("102-B", 1L)).thenReturn(false);
        when(quartoRepository.save(quarto)).thenReturn(quarto);

        QuartoDTO resultado = quartoService.atualizar(1L, new QuartoUpdateDTO("102-B", 2, 6));

        assertEquals("102-B", resultado.getNumeroIdentificacao());
        assertEquals(2, resultado.getAndar());
        assertEquals(6, resultado.getCapacidadeMaxima());
    }

    @Test
    @DisplayName("Não reduz a capacidade abaixo da ocupação atual")
    void atualizar_DeveLancarExcecao_QuandoCapacidadeMenorQueOcupacao() {
        Quarto cheio = new Enfermaria(1L, "101-A", 1, 4, 3, StatusQuarto.DISPONIVEL);
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(cheio));

        assertThrows(RegraNegocioException.class, () -> quartoService.atualizar(1L, new QuartoUpdateDTO(null, null, 2)));
        verify(quartoRepository, never()).save(any());
    }

    @Test
    void atualizar_DeveLancarExcecao_QuandoNumeroPertenceAOutroQuarto() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(quartoRepository.existsByNumeroIdentificacaoAndIdNot("UTI-1", 1L)).thenReturn(true);
        assertThrows(RegraNegocioException.class, () -> quartoService.atualizar(1L, new QuartoUpdateDTO("UTI-1", null, null)));
    }

    @Test
    @DisplayName("Coloca em manutenção e libera")
    void manutencao_ComSucesso() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(quartoRepository.save(quarto)).thenReturn(quarto);

        assertEquals(StatusQuarto.EM_MANUTENCAO, quartoService.colocarEmManutencao(1L).getSituacao());
        assertEquals(StatusQuarto.DISPONIVEL, quartoService.liberarDaManutencao(1L).getSituacao());
    }

    @Test
    @DisplayName("Exclui quarto que nunca teve internações")
    void excluir_ComSucesso() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(internacaoRepository.existsByQuartoId(1L)).thenReturn(false);

        quartoService.excluir(1L);

        verify(quartoRepository).delete(quarto);
    }

    @Test
    @DisplayName("Não exclui quarto com internações registradas")
    void excluir_DeveLancarExcecao_QuandoPossuiInternacoes() {
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(internacaoRepository.existsByQuartoId(1L)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> quartoService.excluir(1L));
        verify(quartoRepository, never()).delete(any());
    }

    @Test
    void listarInternacoesAtivas_ComSucesso() {
        Paciente paciente = new Paciente(1L, "Ana", "529.982.247-25", java.time.LocalDate.of(1980, 1, 1), null, null, null);
        ProfissionalSaude medico = new ClinicoGeral(1L, "Dr. Paulo", "CRM-1", null, null);
        Internacao ativa = new Internacao(5L, paciente, medico, quarto, LocalDateTime.now(), null, null, null, StatusInternacao.EM_ANDAMENTO);
        when(quartoRepository.findById(1L)).thenReturn(Optional.of(quarto));
        when(internacaoRepository.findByQuartoIdAndStatus(1L, StatusInternacao.EM_ANDAMENTO)).thenReturn(List.of(ativa));

        assertEquals(5L, quartoService.listarInternacoesAtivas(1L).get(0).getId());
    }

    @Test
    @DisplayName("Resumo de ocupação soma leitos por tipo e ignora leitos em manutenção")
    void resumoOcupacao() {
        Quarto enfermaria = new Enfermaria(1L, "101", 1, 4, 3, StatusQuarto.DISPONIVEL);
        Quarto uti = new UnidadeTerapiaIntensiva(2L, "UTI-1", 3, 2, 2, StatusQuarto.OCUPADO);
        Quarto emManutencao = new Apartamento(3L, "AP-1", 2, 2, 0, StatusQuarto.EM_MANUTENCAO, true);
        when(quartoRepository.findAll()).thenReturn(List.of(enfermaria, uti, emManutencao));

        OcupacaoHospitalarDTO resumo = quartoService.resumoOcupacao();

        assertEquals(3, resumo.totalQuartos());
        assertEquals(1, resumo.quartosEmManutencao());
        assertEquals(8, resumo.totalLeitos());
        assertEquals(5, resumo.leitosOcupados());
        assertEquals(1, resumo.leitosDisponiveis());
        assertEquals(62.5, resumo.taxaOcupacaoPercentual());
        OcupacaoHospitalarDTO.PorTipo porUti = resumo.porTipo().stream()
                .filter(t -> t.tipo() == TipoQuarto.UTI).findFirst().orElseThrow();
        assertEquals(2, porUti.leitosOcupados());
        assertEquals(0, porUti.leitosDisponiveis());
    }
}
