package com.hospital.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.QuartoDTO;
import com.hospital.entidades.StatusQuarto;
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

    @Test
    @DisplayName("Deve cadastrar quarto com sucesso e retornar 201 Created")
    void deveCadastrarQuartoComSucesso() throws Exception {
        QuartoDTO request = new QuartoDTO();
        request.setNumeroIdentificacao("Q-101");
        request.setAndar(1);
        request.setCapacidadeMaxima(3);
        request.setOcupacaoAtual(0);
        request.setSituacao(StatusQuarto.DISPONIVEL);

        QuartoDTO salvo = new QuartoDTO();
        salvo.setId(1L);
        salvo.setNumeroIdentificacao("Q-101");
        salvo.setAndar(1);
        salvo.setCapacidadeMaxima(3);
        salvo.setOcupacaoAtual(0);
        salvo.setSituacao(StatusQuarto.DISPONIVEL);

        when(quartoService.cadastrarQuarto(any(QuartoDTO.class))).thenReturn(salvo);

        mockMvc.perform(post("/quartos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.numeroIdentificacao").value("Q-101"))
                .andExpect(jsonPath("$.capacidadeMaxima").value(3));
    }

    @Test
    @DisplayName("Deve listar quartos disponiveis")
    void deveListarQuartosDisponiveis() throws Exception {
        QuartoDTO q = new QuartoDTO();
        q.setId(1L);
        q.setNumeroIdentificacao("Q-101");
        q.setCapacidadeMaxima(2);
        q.setOcupacaoAtual(0);
        q.setSituacao(StatusQuarto.DISPONIVEL);

        when(quartoService.listarDisponiveis()).thenReturn(List.of(q));

        mockMvc.perform(get("/quartos/disponiveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].numeroIdentificacao").value("Q-101"));
    }
}
