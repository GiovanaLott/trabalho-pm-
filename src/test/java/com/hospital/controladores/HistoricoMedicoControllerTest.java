package com.hospital.controladores;

import com.hospital.dtos.HistoricoMedicoDTO;
import com.hospital.dtos.PacienteDTO;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.servicos.HistoricoMedicoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoricoMedicoController.class)
class HistoricoMedicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HistoricoMedicoService historicoMedicoService;

    @Test
    @DisplayName("Deve retornar historico medico do paciente com sucesso")
    void deveRetornarHistoricoMedico() throws Exception {
        PacienteDTO paciente = new PacienteDTO(1L, "Carlos", "111.222.333-44", LocalDate.of(1980, 1, 1), null, null, null);
        HistoricoMedicoDTO historico = new HistoricoMedicoDTO(paciente, new ArrayList<>(), new ArrayList<>());

        when(historicoMedicoService.buscarHistoricoPorPaciente(1L)).thenReturn(historico);

        mockMvc.perform(get("/historico/paciente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paciente.nome").value("Carlos"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando o paciente nao existir ao consultar historico")
    void deveRetornar404QuandoPacienteNaoExistir() throws Exception {
        when(historicoMedicoService.buscarHistoricoPorPaciente(999L))
                .thenThrow(new RecursoNaoEncontradoException("Paciente não encontrado com ID: 999"));

        mockMvc.perform(get("/historico/paciente/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"));
    }
}
