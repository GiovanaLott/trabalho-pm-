package com.hospital.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.ProfissionalSaudeDTO;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.servicos.ProfissionalSaudeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfissionalController.class)
class ProfissionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfissionalSaudeService profissionalSaudeService;

    @Test
    @DisplayName("Deve cadastrar profissional com sucesso e retornar 201 Created")
    void deveCadastrarProfissionalComSucesso() throws Exception {
        ProfissionalSaudeDTO dto = new ProfissionalSaudeDTO();
        dto.setNome("Dr. Lucas");
        dto.setRegistroProfissional("CRM-MG-12345");
        dto.setEspecialidade("Cardiologia");
        dto.setEmail("lucas@hospital.com");
        dto.setTelefone("31988881111");

        ProfissionalSaudeDTO salvo = new ProfissionalSaudeDTO();
        salvo.setId(1L);
        salvo.setNome("Dr. Lucas");
        salvo.setRegistroProfissional("CRM-MG-12345");
        salvo.setEspecialidade("Cardiologia");

        when(profissionalSaudeService.cadastrar(any(ProfissionalSaudeDTO.class))).thenReturn(salvo);

        mockMvc.perform(post("/profissionais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Dr. Lucas"))
                .andExpect(jsonPath("$.registroProfissional").value("CRM-MG-12345"));
    }

    @Test
    @DisplayName("Deve listar todos os profissionais de saude")
    void deveListarTodosOsProfissionais() throws Exception {
        ProfissionalSaudeDTO p = new ProfissionalSaudeDTO();
        p.setId(1L);
        p.setNome("Dra. Ana");
        p.setRegistroProfissional("CRM-MG-99999");
        p.setEspecialidade("Pediatria");

        when(profissionalSaudeService.listarTodos()).thenReturn(List.of(p));

        mockMvc.perform(get("/profissionais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Dra. Ana"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando o profissional nao for encontrado")
    void deveRetornar404ProfissionalNaoEncontrado() throws Exception {
        when(profissionalSaudeService.buscarPorId(50L))
                .thenThrow(new RecursoNaoEncontradoException("Profissional de saúde não encontrado com ID: 50"));

        mockMvc.perform(get("/profissionais/50"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"));
    }
}
