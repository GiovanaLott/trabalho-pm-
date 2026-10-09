package com.hospital.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.QuartoDTO;
import com.hospital.entidades.StatusQuarto;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.servicos.QuartoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private QuartoDTO quarto(Long id, String numero, int capacidade, int ocupacao, StatusQuarto situacao) {
        QuartoDTO dto = new QuartoDTO();
        dto.setId(id);
        dto.setNumeroIdentificacao(numero);
        dto.setAndar(1);
        dto.setCapacidadeMaxima(capacidade);
        dto.setOcupacaoAtual(ocupacao);
        dto.setSituacao(situacao);
        return dto;
    }

    @Test
    void cadastrar_comDadosValidos_retorna201() throws Exception {
        when(quartoService.cadastrarQuarto(any(QuartoDTO.class)))
                .thenReturn(quarto(1L, "101-A", 2, 0, StatusQuarto.DISPONIVEL));

        mockMvc.perform(post("/quartos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(quarto(null, "101-A", 2, 0, StatusQuarto.DISPONIVEL))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.situacao").value("DISPONIVEL"));
    }

    @Test
    void cadastrar_comCapacidadeZero_retorna400() throws Exception {
        String corpo = "{\"numeroIdentificacao\":\"101-A\",\"andar\":1,\"capacidadeMaxima\":0}";

        mockMvc.perform(post("/quartos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.capacidadeMaxima").exists());

        verifyNoInteractions(quartoService);
    }

    @Test
    void cadastrar_semNumeroNemAndar_retorna400() throws Exception {
        mockMvc.perform(post("/quartos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"capacidadeMaxima\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.numeroIdentificacao").exists())
                .andExpect(jsonPath("$.errors.andar").exists());
    }

    @Test
    void listarDisponiveis_retorna200() throws Exception {
        when(quartoService.listarDisponiveis())
                .thenReturn(List.of(quarto(1L, "101-A", 2, 1, StatusQuarto.DISPONIVEL)));

        mockMvc.perform(get("/quartos/disponiveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numeroIdentificacao").value("101-A"));
    }

    @Test
    void buscarPorId_inexistente_retorna404() throws Exception {
        when(quartoService.buscarPorId(8L))
                .thenThrow(new RecursoNaoEncontradoException("Quarto nao encontrado com ID: 8"));

        mockMvc.perform(get("/quartos/8"))
                .andExpect(status().isNotFound());
    }
}
