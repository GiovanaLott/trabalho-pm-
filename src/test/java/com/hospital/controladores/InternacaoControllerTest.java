package com.hospital.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.InternacaoDTO;
import com.hospital.dtos.InternacaoRequestDTO;
import com.hospital.entidades.StatusInternacao;
import com.hospital.excecoes.QuartoLotadoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.servicos.InternacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternacaoController.class)
class InternacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InternacaoService internacaoService;

    private InternacaoRequestDTO requisicaoValida() {
        return new InternacaoRequestDTO(1L, 2L, 3L, null, LocalDate.of(2030, 7, 1), "Pos-operatorio");
    }

    private InternacaoDTO internacao(Long id, StatusInternacao status) {
        InternacaoDTO dto = new InternacaoDTO();
        dto.setId(id);
        dto.setStatus(status);
        dto.setObservacoes("Pos-operatorio");
        return dto;
    }

    @Test
    void internar_comDadosValidos_retorna201() throws Exception {
        when(internacaoService.realizarInternacao(any(InternacaoRequestDTO.class)))
                .thenReturn(internacao(5L, StatusInternacao.EM_ANDAMENTO));

        mockMvc.perform(post("/internacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicaoValida())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    @Test
    void internar_semPacienteProfissionalEQuarto_retorna400() throws Exception {
        mockMvc.perform(post("/internacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.pacienteId").exists())
                .andExpect(jsonPath("$.errors.profissionalId").exists())
                .andExpect(jsonPath("$.errors.quartoId").exists());

        verifyNoInteractions(internacaoService);
    }

    @Test
    void internar_emQuartoLotado_retorna409() throws Exception {
        when(internacaoService.realizarInternacao(any(InternacaoRequestDTO.class)))
                .thenThrow(new QuartoLotadoException("O quarto 101-A atingiu sua capacidade maxima."));

        mockMvc.perform(post("/internacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicaoValida())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("O quarto 101-A atingiu sua capacidade maxima."));
    }

    @Test
    void internar_pacienteJaInternado_retorna400() throws Exception {
        when(internacaoService.realizarInternacao(any(InternacaoRequestDTO.class)))
                .thenThrow(new RegraNegocioException("O paciente ja possui uma internacao em andamento."));

        mockMvc.perform(post("/internacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicaoValida())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void darAlta_retorna200ComStatusAlta() throws Exception {
        when(internacaoService.darAlta(5L)).thenReturn(internacao(5L, StatusInternacao.ALTA));

        mockMvc.perform(put("/internacoes/5/alta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ALTA"));
    }

    @Test
    void darAlta_internacaoJaEncerrada_retorna400() throws Exception {
        when(internacaoService.darAlta(5L))
                .thenThrow(new RegraNegocioException("Apenas internacoes em andamento podem receber alta."));

        mockMvc.perform(put("/internacoes/5/alta"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarEmAndamento_retorna200() throws Exception {
        when(internacaoService.listarEmAndamento())
                .thenReturn(List.of(internacao(5L, StatusInternacao.EM_ANDAMENTO)));

        mockMvc.perform(get("/internacoes/em-andamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
