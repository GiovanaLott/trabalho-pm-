package com.hospital.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.ConsultaDTO;
import com.hospital.dtos.ConsultaRequestDTO;
import com.hospital.dtos.PacienteDTO;
import com.hospital.dtos.ProfissionalSaudeDTO;
import com.hospital.entidades.StatusConsulta;
import com.hospital.excecoes.ChoqueHorarioException;
import com.hospital.servicos.ConsultaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsultaController.class)
class ConsultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConsultaService consultaService;

    @Test
    @DisplayName("Deve agendar consulta com sucesso e retornar 201 Created")
    void deveAgendarConsultaComSucesso() throws Exception {
        LocalDateTime data = LocalDateTime.of(2026, 11, 20, 14, 0);
        ConsultaRequestDTO request = new ConsultaRequestDTO(1L, 2L, data, "Rotina", null);

        ConsultaDTO response = new ConsultaDTO();
        response.setId(10L);
        PacienteDTO paciente = new PacienteDTO();
        paciente.setId(1L);
        paciente.setNome("Maria");
        response.setPaciente(paciente);

        ProfissionalSaudeDTO profissional = new ProfissionalSaudeDTO();
        profissional.setId(2L);
        profissional.setNome("Dr. Lucas");
        response.setProfissional(profissional);

        response.setDataHora(data);
        response.setMotivoConsulta("Rotina");
        response.setStatus(StatusConsulta.AGENDADA);

        when(consultaService.agendarConsulta(any(ConsultaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.status").value("AGENDADA"))
                .andExpect(jsonPath("$.paciente.nome").value("Maria"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar agendar consulta em horario conflitante")
    void deveRetornarErroQuandoHouverChoqueDeHorario() throws Exception {
        LocalDateTime data = LocalDateTime.of(2026, 11, 20, 14, 0);
        ConsultaRequestDTO request = new ConsultaRequestDTO(1L, 2L, data, "Rotina", null);

        when(consultaService.agendarConsulta(any(ConsultaRequestDTO.class)))
                .thenThrow(new ChoqueHorarioException("O profissional de saúde já possui uma consulta agendada próxima a este horário."));

        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Violação de regra de negócio"))
                .andExpect(jsonPath("$.message").value("O profissional de saúde já possui uma consulta agendada próxima a este horário."));
    }

    @Test
    @DisplayName("Deve cancelar consulta e retornar status 200 OK")
    void deveCancelarConsultaComSucesso() throws Exception {
        ConsultaDTO response = new ConsultaDTO();
        response.setId(5L);
        response.setStatus(StatusConsulta.CANCELADA);

        when(consultaService.cancelarConsulta(5L)).thenReturn(response);

        mockMvc.perform(put("/consultas/5/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    @DisplayName("Deve listar consultas por paciente")
    void deveListarConsultasPorPaciente() throws Exception {
        ConsultaDTO c = new ConsultaDTO();
        c.setId(1L);

        when(consultaService.buscarPorPaciente(3L)).thenReturn(List.of(c));

        mockMvc.perform(get("/consultas/paciente/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
