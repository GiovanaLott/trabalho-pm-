package com.hospital.controladores;

import com.hospital.dtos.ConsultaDTO;
import com.hospital.dtos.HistoricoMedicoDTO;
import com.hospital.dtos.InternacaoDTO;
import com.hospital.dtos.PacienteDTO;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.servicos.HistoricoMedicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
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
    void historicoDoPaciente_retorna200ComConsultasEInternacoes() throws Exception {
        PacienteDTO paciente = new PacienteDTO();
        paciente.setId(1L);
        paciente.setNome("Joao Silva");
        HistoricoMedicoDTO historico = new HistoricoMedicoDTO(paciente,
                List.of(new ConsultaDTO(), new ConsultaDTO()),
                List.of(new InternacaoDTO()));
        when(historicoMedicoService.buscarHistoricoPorPaciente(1L)).thenReturn(historico);

        mockMvc.perform(get("/historico/paciente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paciente.nome").value("Joao Silva"))
                .andExpect(jsonPath("$.consultas", hasSize(2)))
                .andExpect(jsonPath("$.internacoes", hasSize(1)));
    }

    @Test
    void historicoDePacienteInexistente_retorna404() throws Exception {
        when(historicoMedicoService.buscarHistoricoPorPaciente(42L))
                .thenThrow(new RecursoNaoEncontradoException("Paciente nao encontrado com ID: 42"));

        mockMvc.perform(get("/historico/paciente/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Paciente nao encontrado com ID: 42"));
    }
}
