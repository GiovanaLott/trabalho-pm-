package com.hospital.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.PacienteDTO;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.servicos.PacienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PacienteController.class)
class PacienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PacienteService pacienteService;

    @Test
    @DisplayName("Deve cadastrar paciente com sucesso e retornar 201 Created")
    void deveCadastrarPacienteComSucesso() throws Exception {
        PacienteDTO dto = new PacienteDTO(null, "Maria Silva", "123.456.789-00", LocalDate.of(1990, 5, 10), "31999998888", "Rua A, 100", "maria@email.com");
        PacienteDTO salvo = new PacienteDTO(1L, "Maria Silva", "123.456.789-00", LocalDate.of(1990, 5, 10), "31999998888", "Rua A, 100", "maria@email.com");

        when(pacienteService.cadastrar(any(PacienteDTO.class))).thenReturn(salvo);

        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.cpf").value("123.456.789-00"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar cadastrar paciente com dados invalidos")
    void deveRetornarErro400AoCadastrarPacienteInvalido() throws Exception {
        PacienteDTO dtoInvalido = new PacienteDTO(null, "", "", null, null, null, "email-invalido");

        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Erro de validação dos campos"));
    }

    @Test
    @DisplayName("Deve listar todos os pacientes e retornar 200 OK")
    void deveListarPacientes() throws Exception {
        PacienteDTO p1 = new PacienteDTO(1L, "Joao", "111.222.333-44", LocalDate.of(1985, 2, 1), "31988887777", null, null);
        when(pacienteService.listarTodos()).thenReturn(List.of(p1));

        mockMvc.perform(get("/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Joao"));
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found para paciente inexistente")
    void deveRetornar404ParaPacienteNaoEncontrado() throws Exception {
        when(pacienteService.buscarPorId(99L)).thenThrow(new RecursoNaoEncontradoException("Paciente não encontrado com ID: 99"));

        mockMvc.perform(get("/pacientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.message").value("Paciente não encontrado com ID: 99"));
    }
}
