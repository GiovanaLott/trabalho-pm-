package com.hospital.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.PacienteDTO;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.servicos.PacienteService;
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

    private PacienteDTO pacienteValido() {
        PacienteDTO dto = new PacienteDTO();
        dto.setNome("Joao Silva");
        dto.setCpf("123.456.789-00");
        dto.setDataNascimento(LocalDate.of(1990, 5, 20));
        dto.setTelefone("31999998888");
        dto.setEndereco("Rua A, 100");
        dto.setEmail("joao@email.com");
        return dto;
    }

    @Test
    void cadastrar_comDadosValidos_retorna201ComOPacienteSalvo() throws Exception {
        PacienteDTO salvo = pacienteValido();
        salvo.setId(1L);
        when(pacienteService.cadastrar(any(PacienteDTO.class))).thenReturn(salvo);

        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pacienteValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cpf").value("123.456.789-00"))
                .andExpect(jsonPath("$.dataNascimento").value("1990-05-20"));
    }

    @Test
    void cadastrar_comCamposInvalidos_retorna400DetalhandoCadaCampo() throws Exception {
        String corpoInvalido = "{\"nome\":\"\",\"cpf\":\"\",\"email\":\"isto-nao-e-email\"}";

        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.nome").exists())
                .andExpect(jsonPath("$.errors.cpf").exists())
                .andExpect(jsonPath("$.errors.dataNascimento").exists())
                .andExpect(jsonPath("$.errors.email").exists());

        verifyNoInteractions(pacienteService);
    }

    @Test
    void cadastrar_comCpfJaCadastrado_retorna400() throws Exception {
        when(pacienteService.cadastrar(any(PacienteDTO.class)))
                .thenThrow(new RegraNegocioException("Ja existe um paciente cadastrado com o CPF informado."));

        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pacienteValido())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ja existe um paciente cadastrado com o CPF informado."));
    }

    @Test
    void listarTodos_retorna200ComALista() throws Exception {
        PacienteDTO um = pacienteValido();
        um.setId(1L);
        PacienteDTO dois = pacienteValido();
        dois.setId(2L);
        when(pacienteService.listarTodos()).thenReturn(List.of(um, dois));

        mockMvc.perform(get("/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void buscarPorId_existente_retorna200() throws Exception {
        PacienteDTO dto = pacienteValido();
        dto.setId(7L);
        when(pacienteService.buscarPorId(7L)).thenReturn(dto);

        mockMvc.perform(get("/pacientes/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nome").value("Joao Silva"));
    }

    @Test
    void buscarPorId_inexistente_retorna404() throws Exception {
        when(pacienteService.buscarPorId(99L))
                .thenThrow(new RecursoNaoEncontradoException("Paciente nao encontrado com ID: 99"));

        mockMvc.perform(get("/pacientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Paciente nao encontrado com ID: 99"))
                .andExpect(jsonPath("$.path").value("/pacientes/99"));
    }
}
