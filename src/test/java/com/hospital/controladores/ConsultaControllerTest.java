package com.hospital.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.ConsultaDTO;
import com.hospital.dtos.ConsultaRequestDTO;
import com.hospital.entidades.StatusConsulta;
import com.hospital.excecoes.ChoqueHorarioException;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.servicos.ConsultaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsultaController.class)
class ConsultaControllerTest {

    private static final LocalDateTime DATA_HORA = LocalDateTime.of(2030, 6, 10, 14, 30);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConsultaService consultaService;

    private ConsultaRequestDTO requisicaoValida() {
        return new ConsultaRequestDTO(1L, 2L, DATA_HORA, "Rotina", null);
    }

    private ConsultaDTO consulta(Long id, StatusConsulta status) {
        ConsultaDTO dto = new ConsultaDTO();
        dto.setId(id);
        dto.setDataHora(DATA_HORA);
        dto.setMotivoConsulta("Rotina");
        dto.setStatus(status);
        return dto;
    }

    @Test
    void agendar_comDadosValidos_retorna201() throws Exception {
        when(consultaService.agendarConsulta(any(ConsultaRequestDTO.class)))
                .thenReturn(consulta(10L, StatusConsulta.AGENDADA));

        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicaoValida())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("AGENDADA"));
    }

    @Test
    void agendar_semCamposObrigatorios_retorna400() throws Exception {
        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivoConsulta\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.pacienteId").exists())
                .andExpect(jsonPath("$.errors.profissionalId").exists())
                .andExpect(jsonPath("$.errors.dataHora").exists())
                .andExpect(jsonPath("$.errors.motivoConsulta").exists());

        verifyNoInteractions(consultaService);
    }

    @Test
    void agendar_comChoqueDeHorario_retorna409() throws Exception {
        when(consultaService.agendarConsulta(any(ConsultaRequestDTO.class)))
                .thenThrow(new ChoqueHorarioException("O profissional ja possui uma consulta proxima a esse horario."));

        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicaoValida())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("O profissional ja possui uma consulta proxima a esse horario."));
    }

    @Test
    void agendar_comPacienteInexistente_retorna404() throws Exception {
        when(consultaService.agendarConsulta(any(ConsultaRequestDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Paciente nao encontrado com ID: 1"));

        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicaoValida())))
                .andExpect(status().isNotFound());
    }

    @Test
    void agendar_comDataNoPassado_retorna400() throws Exception {
        when(consultaService.agendarConsulta(any(ConsultaRequestDTO.class)))
                .thenThrow(new RegraNegocioException("Nao e possivel agendar consultas para datas passadas."));

        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicaoValida())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void agendar_comDataEmFormatoInvalido_retorna400() throws Exception {
        String corpo = "{\"pacienteId\":1,\"profissionalId\":2,\"dataHora\":\"10/06/2030 14:30\",\"motivoConsulta\":\"Rotina\"}";

        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(consultaService);
    }

    @Test
    void listarPorPaciente_retorna200() throws Exception {
        when(consultaService.buscarPorPaciente(1L))
                .thenReturn(List.of(consulta(1L, StatusConsulta.AGENDADA), consulta(2L, StatusConsulta.REALIZADA)));

        mockMvc.perform(get("/consultas/paciente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void cancelar_retorna200ComStatusCancelada() throws Exception {
        when(consultaService.cancelarConsulta(10L)).thenReturn(consulta(10L, StatusConsulta.CANCELADA));

        mockMvc.perform(put("/consultas/10/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    void cancelar_consultaJaCancelada_retorna400() throws Exception {
        when(consultaService.cancelarConsulta(10L))
                .thenThrow(new RegraNegocioException("Esta consulta ja esta cancelada."));

        mockMvc.perform(put("/consultas/10/cancelar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void finalizar_repassaAsObservacoesMedicas() throws Exception {
        when(consultaService.finalizarConsulta(10L, "Paciente estavel"))
                .thenReturn(consulta(10L, StatusConsulta.REALIZADA));

        mockMvc.perform(put("/consultas/10/finalizar").param("observacoesMedicas", "Paciente estavel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REALIZADA"));

        verify(consultaService).finalizarConsulta(10L, "Paciente estavel");
    }
}
