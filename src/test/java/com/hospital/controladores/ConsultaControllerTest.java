package com.hospital.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.ConsultaDTO;
import com.hospital.dtos.ConsultaRequestDTO;
import com.hospital.dtos.PacienteDTO;
import com.hospital.dtos.ProfissionalSaudeDTO;
import com.hospital.entidades.ModalidadeAtendimento;
import com.hospital.entidades.PrioridadeAtendimento;
import com.hospital.entidades.SituacaoConsulta;
import com.hospital.entidades.StatusConsulta;
import com.hospital.excecoes.ChoqueHorarioException;
import com.hospital.excecoes.RegraNegocioException;
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
import static org.mockito.ArgumentMatchers.eq;
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
        response.setSituacao(SituacaoConsulta.AGENDADA);
        response.setModalidade(ModalidadeAtendimento.ROTINA);

        when(consultaService.agendarConsulta(any(ConsultaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.situacao").value("AGENDADA"))
                .andExpect(jsonPath("$.modalidade").value("ROTINA"))
                .andExpect(jsonPath("$.paciente.nome").value("Maria"));
    }

    @Test
    @DisplayName("Deve agendar consulta especializada com especialidade informada")
    void deveAgendarConsultaEspecializadaComSucesso() throws Exception {
        LocalDateTime data = LocalDateTime.of(2026, 11, 25, 10, 30);
        ConsultaRequestDTO request = new ConsultaRequestDTO(1L, 2L, data, "Eletrocardiograma", null,
                ModalidadeAtendimento.ESPECIALIZADA, "Cardiologia", null);

        ConsultaDTO response = new ConsultaDTO();
        response.setId(12L);
        response.setModalidade(ModalidadeAtendimento.ESPECIALIZADA);
        response.setEspecialidade("Cardiologia");
        response.setSituacao(SituacaoConsulta.AGENDADA);

        when(consultaService.agendarConsulta(any(ConsultaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12L))
                .andExpect(jsonPath("$.modalidade").value("ESPECIALIZADA"))
                .andExpect(jsonPath("$.especialidade").value("Cardiologia"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar agendar consulta em horário conflitante")
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
    @DisplayName("Deve confirmar consulta agendada e retornar 200 OK")
    void deveConfirmarConsultaComSucesso() throws Exception {
        ConsultaDTO response = new ConsultaDTO();
        response.setId(5L);
        response.setSituacao(SituacaoConsulta.CONFIRMADA);

        when(consultaService.confirmarConsulta(5L)).thenReturn(response);

        mockMvc.perform(put("/consultas/5/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CONFIRMADA"));
    }

    @Test
    @DisplayName("Deve iniciar atendimento de consulta e retornar 200 OK")
    void deveIniciarAtendimentoComSucesso() throws Exception {
        ConsultaDTO response = new ConsultaDTO();
        response.setId(5L);
        response.setSituacao(SituacaoConsulta.EM_ATENDIMENTO);

        when(consultaService.iniciarAtendimento(5L)).thenReturn(response);

        mockMvc.perform(put("/consultas/5/iniciar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("EM_ATENDIMENTO"));
    }

    @Test
    @DisplayName("Deve concluir consulta com observações e retornar 200 OK")
    void deveConcluirConsultaComSucesso() throws Exception {
        ConsultaDTO response = new ConsultaDTO();
        response.setId(5L);
        response.setSituacao(SituacaoConsulta.CONCLUIDA);
        response.setObservacoesMedicas("Paciente medicado.");

        when(consultaService.finalizarConsulta(eq(5L), eq("Paciente medicado."))).thenReturn(response);

        mockMvc.perform(put("/consultas/5/concluir")
                        .param("observacoesMedicas", "Paciente medicado."))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CONCLUIDA"))
                .andExpect(jsonPath("$.observacoesMedicas").value("Paciente medicado."));
    }

    @Test
    @DisplayName("Deve registrar não comparecimento e retornar 200 OK")
    void deveRegistrarNaoComparecimentoComSucesso() throws Exception {
        ConsultaDTO response = new ConsultaDTO();
        response.setId(5L);
        response.setSituacao(SituacaoConsulta.NAO_COMPARECEU);

        when(consultaService.registrarNaoComparecimento(5L)).thenReturn(response);

        mockMvc.perform(put("/consultas/5/nao-compareceu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("NAO_COMPARECEU"));
    }

    @Test
    @DisplayName("Deve cancelar consulta e retornar status 200 OK")
    void deveCancelarConsultaComSucesso() throws Exception {
        ConsultaDTO response = new ConsultaDTO();
        response.setId(5L);
        response.setSituacao(SituacaoConsulta.CANCELADA);

        when(consultaService.cancelarConsulta(5L)).thenReturn(response);

        mockMvc.perform(put("/consultas/5/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CANCELADA"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar transição inválida de situação")
    void deveRetornarErroAoTentarTransicaoInvalida() throws Exception {
        when(consultaService.confirmarConsulta(5L))
                .thenThrow(new RegraNegocioException("Uma consulta cancelada não pode ser alterada sem um novo agendamento."));

        mockMvc.perform(put("/consultas/5/confirmar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Uma consulta cancelada não pode ser alterada sem um novo agendamento."));
    }

    @Test
    @DisplayName("Deve listar consultas por situação")
    void deveListarConsultasPorSituacao() throws Exception {
        ConsultaDTO c = new ConsultaDTO();
        c.setId(1L);
        c.setSituacao(SituacaoConsulta.CONFIRMADA);

        when(consultaService.buscarPorSituacao(SituacaoConsulta.CONFIRMADA)).thenReturn(List.of(c));

        mockMvc.perform(get("/consultas/situacao/CONFIRMADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].situacao").value("CONFIRMADA"));
    }

    @Test
    @DisplayName("Deve listar emergências que aguardam avaliação")
    void deveListarEmergenciasAguardandoAvaliacao() throws Exception {
        ConsultaDTO c = new ConsultaDTO();
        c.setId(1L);
        c.setModalidade(ModalidadeAtendimento.EMERGENCIA);
        c.setPrioridade(PrioridadeAtendimento.ALTA);
        c.setAguardandoAvaliacao(true);

        when(consultaService.buscarEmergenciasAguardando()).thenReturn(List.of(c));

        mockMvc.perform(get("/consultas/emergencias/aguardando"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].modalidade").value("EMERGENCIA"))
                .andExpect(jsonPath("$[0].aguardandoAvaliacao").value(true));
    }
}
