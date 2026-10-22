package com.hospital.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.ConclusaoCirurgiaDTO;
import com.hospital.dtos.ProcedimentoCirurgicoDTO;
import com.hospital.dtos.ProcedimentoCirurgicoRequestDTO;
import com.hospital.entidades.StatusCirurgia;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.servicos.ProcedimentoCirurgicoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcedimentoCirurgicoController.class)
class ProcedimentoCirurgicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProcedimentoCirurgicoService procedimentoService;

    @Test
    @DisplayName("Deve agendar procedimento cirurgico e retornar 201 Created")
    void deveAgendarProcedimentoComSucesso() throws Exception {
        ProcedimentoCirurgicoRequestDTO request = new ProcedimentoCirurgicoRequestDTO(
                "Apendicectomia", 1L, 2L, LocalDate.now().plusDays(3), LocalTime.of(8, 0), "Jejum 8h"
        );

        ProcedimentoCirurgicoDTO response = new ProcedimentoCirurgicoDTO();
        response.setId(10L);
        response.setDescricao("Apendicectomia");
        response.setSituacao(StatusCirurgia.AGENDADA);

        when(procedimentoService.agendar(any(ProcedimentoCirurgicoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/procedimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.descricao").value("Apendicectomia"))
                .andExpect(jsonPath("$.situacao").value("AGENDADA"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao agendar cirurgia com dados inválidos")
    void deveRetornarErro400AoAgendarCirurgiaInvalida() throws Exception {
        ProcedimentoCirurgicoRequestDTO requestInvalido = new ProcedimentoCirurgicoRequestDTO(
                "", null, null, null, null, null
        );

        mockMvc.perform(post("/procedimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Erro de validação dos campos"));
    }

    @Test
    @DisplayName("Deve iniciar cirurgia com sucesso")
    void deveIniciarCirurgiaComSucesso() throws Exception {
        ProcedimentoCirurgicoDTO response = new ProcedimentoCirurgicoDTO();
        response.setId(10L);
        response.setSituacao(StatusCirurgia.EM_ANDAMENTO);

        when(procedimentoService.iniciarCirurgia(10L)).thenReturn(response);

        mockMvc.perform(put("/procedimentos/10/iniciar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("EM_ANDAMENTO"));
    }

    @Test
    @DisplayName("Deve concluir cirurgia iniciada com sucesso")
    void deveConcluirCirurgiaComSucesso() throws Exception {
        ConclusaoCirurgiaDTO conclusao = new ConclusaoCirurgiaDTO(LocalTime.of(11, 0), "Sucesso");

        ProcedimentoCirurgicoDTO response = new ProcedimentoCirurgicoDTO();
        response.setId(10L);
        response.setSituacao(StatusCirurgia.CONCLUIDA);
        response.setObservacoesPosOperatorias("Sucesso");

        when(procedimentoService.concluirCirurgia(eq(10L), any(ConclusaoCirurgiaDTO.class))).thenReturn(response);

        mockMvc.perform(put("/procedimentos/10/concluir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conclusao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CONCLUIDA"))
                .andExpect(jsonPath("$.observacoesPosOperatorias").value("Sucesso"));
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar concluir cirurgia que não foi iniciada")
    void deveRetornarErroAoTentarConcluirCirurgiaNaoIniciada() throws Exception {
        when(procedimentoService.concluirCirurgia(eq(10L), any()))
                .thenThrow(new RegraNegocioException("Uma cirurgia não pode ser concluída se ainda não tiver sido iniciada."));

        mockMvc.perform(put("/procedimentos/10/concluir"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Violação de regra de negócio"))
                .andExpect(jsonPath("$.message").value("Uma cirurgia não pode ser concluída se ainda não tiver sido iniciada."));
    }

    @Test
    @DisplayName("Deve cancelar cirurgia com sucesso")
    void deveCancelarCirurgiaComSucesso() throws Exception {
        ProcedimentoCirurgicoDTO response = new ProcedimentoCirurgicoDTO();
        response.setId(10L);
        response.setSituacao(StatusCirurgia.CANCELADA);

        when(procedimentoService.cancelarCirurgia(10L)).thenReturn(response);

        mockMvc.perform(put("/procedimentos/10/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CANCELADA"));
    }

    @Test
    @DisplayName("Deve listar todos os procedimentos")
    void deveListarProcedimentos() throws Exception {
        ProcedimentoCirurgicoDTO dto = new ProcedimentoCirurgicoDTO();
        dto.setId(1L);
        dto.setDescricao("Cirurgia Geral");

        when(procedimentoService.listarTodos()).thenReturn(List.of(dto));

        mockMvc.perform(get("/procedimentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].descricao").value("Cirurgia Geral"));
    }

    @Test
    @DisplayName("Deve buscar procedimento por ID")
    void deveBuscarProcedimentoPorId() throws Exception {
        ProcedimentoCirurgicoDTO dto = new ProcedimentoCirurgicoDTO();
        dto.setId(1L);
        dto.setDescricao("Cirurgia Geral");

        when(procedimentoService.buscarPorId(1L)).thenReturn(dto);

        mockMvc.perform(get("/procedimentos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.descricao").value("Cirurgia Geral"));
    }
}
