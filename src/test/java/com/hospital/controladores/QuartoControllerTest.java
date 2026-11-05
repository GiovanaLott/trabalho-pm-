package com.hospital.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.InternacaoDTO;
import com.hospital.dtos.OcupacaoHospitalarDTO;
import com.hospital.dtos.QuartoDTO;
import com.hospital.dtos.QuartoUpdateDTO;
import com.hospital.entidades.Enfermaria;
import com.hospital.entidades.StatusQuarto;
import com.hospital.entidades.TipoQuarto;
import com.hospital.entidades.UnidadeTerapiaIntensiva;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.servicos.QuartoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuartoController.class)
class QuartoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuartoService quartoService;

    private final QuartoDTO enfermaria = new QuartoDTO(new Enfermaria(1L, "Q-101", 1, 4, 0, StatusQuarto.DISPONIVEL));
    private final QuartoDTO uti = new QuartoDTO(new UnidadeTerapiaIntensiva(2L, "UTI-1", 3, 2, 2, StatusQuarto.OCUPADO));

    @Test
    @DisplayName("POST /quartos cadastra e retorna 201 com tipo e leitos disponíveis")
    void deveCadastrarQuartoComSucesso() throws Exception {
        QuartoDTO request = new QuartoDTO();
        request.setNumeroIdentificacao("Q-101");
        request.setAndar(1);
        request.setCapacidadeMaxima(4);
        request.setTipo(TipoQuarto.ENFERMARIA);
        when(quartoService.cadastrarQuarto(any(QuartoDTO.class))).thenReturn(enfermaria);

        mockMvc.perform(post("/quartos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tipo").value("ENFERMARIA"))
                .andExpect(jsonPath("$.descricaoTipo").value("Enfermaria"))
                .andExpect(jsonPath("$.leitosDisponiveis").value(4))
                .andExpect(jsonPath("$.situacao").value("DISPONIVEL"));
    }

    @Test
    @DisplayName("POST /quartos com campos inválidos retorna 400")
    void deveRetornar400ParaQuartoInvalido() throws Exception {
        QuartoDTO request = new QuartoDTO();
        request.setNumeroIdentificacao("");
        request.setAndar(-1);
        request.setCapacidadeMaxima(0);

        mockMvc.perform(post("/quartos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.numeroIdentificacao").exists())
                .andExpect(jsonPath("$.errors.andar").exists())
                .andExpect(jsonPath("$.errors.capacidadeMaxima").exists());
        verify(quartoService, never()).cadastrarQuarto(any());
    }

    @Test
    @DisplayName("POST /quartos com tipo inexistente retorna 400")
    void deveRetornar400ParaTipoInexistente() throws Exception {
        mockMvc.perform(post("/quartos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroIdentificacao\":\"X\",\"andar\":1,\"capacidadeMaxima\":2,\"tipo\":\"SUITE_MASTER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /quartos repassa os filtros de tipo, andar e situação")
    void deveListarQuartosComFiltros() throws Exception {
        when(quartoService.listar(TipoQuarto.UTI, 3, StatusQuarto.OCUPADO)).thenReturn(List.of(uti));

        mockMvc.perform(get("/quartos").param("tipo", "UTI").param("andar", "3").param("situacao", "OCUPADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroIdentificacao").value("UTI-1"))
                .andExpect(jsonPath("$[0].leitosDisponiveis").value(0));
    }

    @Test
    void deveListarQuartosDisponiveis() throws Exception {
        when(quartoService.listarDisponiveis(null)).thenReturn(List.of(enfermaria));
        when(quartoService.listarDisponiveis(TipoQuarto.UTI)).thenReturn(List.of());

        mockMvc.perform(get("/quartos/disponiveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].situacao").value("DISPONIVEL"));
        mockMvc.perform(get("/quartos/disponiveis").param("tipo", "UTI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveRetornarResumoDeOcupacao() throws Exception {
        OcupacaoHospitalarDTO resumo = new OcupacaoHospitalarDTO(2, 0, 6, 2, 4, 33.3, List.of());
        when(quartoService.resumoOcupacao()).thenReturn(resumo);

        mockMvc.perform(get("/quartos/ocupacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLeitos").value(6))
                .andExpect(jsonPath("$.taxaOcupacaoPercentual").value(33.3));
    }

    @Test
    void deveBuscarQuartoPorIdEListarInternacoes() throws Exception {
        when(quartoService.buscarPorId(1L)).thenReturn(enfermaria);
        when(quartoService.listarInternacoesAtivas(1L)).thenReturn(List.of(new InternacaoDTO()));

        mockMvc.perform(get("/quartos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroIdentificacao").value("Q-101"));
        mockMvc.perform(get("/quartos/1/internacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deveAtualizarQuarto() throws Exception {
        when(quartoService.atualizar(eq(1L), any(QuartoUpdateDTO.class))).thenReturn(enfermaria);

        mockMvc.perform(put("/quartos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new QuartoUpdateDTO(null, 2, 6))))
                .andExpect(status().isOk());
        verify(quartoService).atualizar(eq(1L), any(QuartoUpdateDTO.class));
    }

    @Test
    void deveColocarEmManutencaoELiberar() throws Exception {
        when(quartoService.colocarEmManutencao(1L)).thenReturn(enfermaria);
        when(quartoService.liberarDaManutencao(1L)).thenReturn(enfermaria);

        mockMvc.perform(put("/quartos/1/manutencao")).andExpect(status().isOk());
        mockMvc.perform(put("/quartos/1/liberar")).andExpect(status().isOk());
    }

    @Test
    void deveRetornar400AoColocarQuartoOcupadoEmManutencao() throws Exception {
        when(quartoService.colocarEmManutencao(2L))
                .thenThrow(new RegraNegocioException("O quarto UTI-1 possui 2 paciente(s) internado(s) e não pode entrar em manutenção."));

        mockMvc.perform(put("/quartos/2/manutencao"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O quarto UTI-1 possui 2 paciente(s) internado(s) e não pode entrar em manutenção."));
    }

    @Test
    void deveExcluirQuarto() throws Exception {
        mockMvc.perform(delete("/quartos/1")).andExpect(status().isNoContent());
        verify(quartoService).excluir(1L);
    }
}
