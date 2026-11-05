package com.hospital.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.PacienteDTO;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PacienteController.class)
class PacienteControllerTest {

    private static final String CPF = "529.982.247-25";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PacienteService pacienteService;

    private PacienteDTO paciente(Long id) {
        return new PacienteDTO(id, "Maria Silva", CPF, LocalDate.of(1990, 5, 10), "(31) 99999-8888", "Rua A, 100", "maria@email.com");
    }

    @Test
    @DisplayName("POST /pacientes cadastra e retorna 201 Created")
    void deveCadastrarPacienteComSucesso() throws Exception {
        when(pacienteService.cadastrar(any(PacienteDTO.class))).thenReturn(paciente(1L));

        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paciente(null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.cpf").value(CPF));
    }

    @Test
    @DisplayName("POST /pacientes com campos inválidos retorna 400 com o erro de cada campo")
    void deveRetornarErro400AoCadastrarPacienteInvalido() throws Exception {
        PacienteDTO dtoInvalido = new PacienteDTO(null, "", "", LocalDate.now().plusDays(1), "abc", null, "email-invalido");

        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nome").exists())
                .andExpect(jsonPath("$.errors.cpf").exists())
                .andExpect(jsonPath("$.errors.dataNascimento").exists())
                .andExpect(jsonPath("$.errors.telefone").exists())
                .andExpect(jsonPath("$.errors.email").exists());

        verify(pacienteService, never()).cadastrar(any());
    }

    @Test
    @DisplayName("POST /pacientes com CPF repetido retorna 400 com a mensagem da regra")
    void deveRetornarErroQuandoCpfDuplicado() throws Exception {
        when(pacienteService.cadastrar(any(PacienteDTO.class)))
                .thenThrow(new RegraNegocioException("Já existe um paciente cadastrado com o CPF: " + CPF));

        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paciente(null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Já existe um paciente cadastrado com o CPF: " + CPF));
    }

    @Test
    @DisplayName("GET /pacientes lista todos e GET /pacientes?nome= filtra")
    void deveListarPacientes() throws Exception {
        when(pacienteService.buscarPorNome(null)).thenReturn(List.of(paciente(1L), paciente(2L)));
        when(pacienteService.buscarPorNome("maria")).thenReturn(List.of(paciente(1L)));

        mockMvc.perform(get("/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/pacientes").param("nome", "maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deveBuscarPacientePorId() throws Exception {
        when(pacienteService.buscarPorId(1L)).thenReturn(paciente(1L));

        mockMvc.perform(get("/pacientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }

    @Test
    void deveRetornar404QuandoPacienteNaoExiste() throws Exception {
        when(pacienteService.buscarPorId(99L)).thenThrow(new RecursoNaoEncontradoException("Paciente não encontrado com ID: 99"));

        mockMvc.perform(get("/pacientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Paciente não encontrado com ID: 99"))
                .andExpect(jsonPath("$.path").value("/pacientes/99"));
    }

    @Test
    @DisplayName("GET /pacientes/cpf/{cpf} aceita CPF com máscara")
    void deveBuscarPacientePorCpf() throws Exception {
        when(pacienteService.buscarPorCpf(CPF)).thenReturn(paciente(1L));

        mockMvc.perform(get("/pacientes/cpf/" + CPF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deveAtualizarPaciente() throws Exception {
        PacienteDTO atualizado = paciente(1L);
        atualizado.setEndereco("Rua Nova, 1");
        when(pacienteService.atualizar(eq(1L), any(PacienteDTO.class))).thenReturn(atualizado);

        mockMvc.perform(put("/pacientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endereco").value("Rua Nova, 1"));
    }

    @Test
    void deveExcluirPacienteSemHistorico() throws Exception {
        mockMvc.perform(delete("/pacientes/1"))
                .andExpect(status().isNoContent());
        verify(pacienteService).excluir(1L);
    }

    @Test
    void deveRetornar400AoExcluirPacienteComHistorico() throws Exception {
        doThrow(new RegraNegocioException("O paciente possui histórico e não pode ser excluído."))
                .when(pacienteService).excluir(1L);

        mockMvc.perform(delete("/pacientes/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Violação de regra de negócio"));
    }
}
