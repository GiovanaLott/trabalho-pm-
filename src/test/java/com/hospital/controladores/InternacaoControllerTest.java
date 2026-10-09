package com.hospital.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.InternacaoDTO;
import com.hospital.dtos.InternacaoRequestDTO;
import com.hospital.dtos.PacienteDTO;
import com.hospital.dtos.QuartoDTO;
import com.hospital.entidades.StatusInternacao;
import com.hospital.excecoes.QuartoLotadoException;
import com.hospital.servicos.InternacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

    @Test
    @DisplayName("Deve registrar internacao com sucesso e retornar 201 Created")
    void deveRegistrarInternacaoComSucesso() throws Exception {
        InternacaoRequestDTO request = new InternacaoRequestDTO(1L, 2L, 3L, LocalDateTime.now(), LocalDate.now().plusDays(3), "Observacao");

        InternacaoDTO response = new InternacaoDTO();
        response.setId(10L);
        PacienteDTO paciente = new PacienteDTO();
        paciente.setId(1L);
        response.setPaciente(paciente);

        QuartoDTO quarto = new QuartoDTO();
        quarto.setId(3L);
        response.setQuarto(quarto);

        response.setDataEntrada(LocalDateTime.now());
        response.setStatus(StatusInternacao.EM_ANDAMENTO);

        when(internacaoService.realizarInternacao(any(InternacaoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/internacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar internar em quarto lotado")
    void deveRetornarErroQuandoQuartoEstiverLotado() throws Exception {
        InternacaoRequestDTO request = new InternacaoRequestDTO(1L, 2L, 3L, LocalDateTime.now(), LocalDate.now().plusDays(3), "Observacao");

        when(internacaoService.realizarInternacao(any(InternacaoRequestDTO.class)))
                .thenThrow(new QuartoLotadoException("O quarto atingiu sua capacidade máxima de leitos."));

        mockMvc.perform(post("/internacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Violação de regra de negócio"))
                .andExpect(jsonPath("$.message").value("O quarto atingiu sua capacidade máxima de leitos."));
    }

    @Test
    @DisplayName("Deve dar alta hospitalar e retornar status 200 OK")
    void deveDarAltaComSucesso() throws Exception {
        InternacaoDTO response = new InternacaoDTO();
        response.setId(10L);
        response.setStatus(StatusInternacao.ALTA);

        when(internacaoService.darAlta(10L)).thenReturn(response);

        mockMvc.perform(put("/internacoes/10/alta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ALTA"));
    }

    @Test
    @DisplayName("Deve listar internacoes em andamento")
    void deveListarInternacoesEmAndamento() throws Exception {
        InternacaoDTO dto = new InternacaoDTO();
        dto.setId(1L);
        when(internacaoService.listarEmAndamento()).thenReturn(List.of(dto));

        mockMvc.perform(get("/internacoes/em-andamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
