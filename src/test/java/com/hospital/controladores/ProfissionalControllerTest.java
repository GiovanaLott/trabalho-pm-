package com.hospital.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.ProfissionalSaudeDTO;
import com.hospital.excecoes.RecursoNaoEncontradoException;
import com.hospital.excecoes.RegraNegocioException;
import com.hospital.servicos.ProfissionalSaudeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
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
    private ProfissionalSaudeService profissionalService;

    private ProfissionalSaudeDTO profissionalValido() {
        ProfissionalSaudeDTO dto = new ProfissionalSaudeDTO();
        dto.setNome("Dra. Maria");
        dto.setRegistroProfissional("CRM12345");
        dto.setEspecialidade("Cardiologia");
        dto.setTelefone("31988887777");
        dto.setEmail("maria@hospital.com");
        return dto;
    }

    @Test
    void cadastrar_comDadosValidos_retorna201() throws Exception {
        ProfissionalSaudeDTO salvo = profissionalValido();
        salvo.setId(3L);
        when(profissionalService.cadastrar(any(ProfissionalSaudeDTO.class))).thenReturn(salvo);

        mockMvc.perform(post("/profissionais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profissionalValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.registroProfissional").value("CRM12345"));
    }

    @Test
    void cadastrar_semCamposObrigatorios_retorna400() throws Exception {
        mockMvc.perform(post("/profissionais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telefone\":\"31988887777\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nome").exists())
                .andExpect(jsonPath("$.errors.registroProfissional").exists())
                .andExpect(jsonPath("$.errors.especialidade").exists());

        verifyNoInteractions(profissionalService);
    }

    @Test
    void cadastrar_comRegistroDuplicado_retorna400() throws Exception {
        when(profissionalService.cadastrar(any(ProfissionalSaudeDTO.class)))
                .thenThrow(new RegraNegocioException("Registro ja cadastrado."));

        mockMvc.perform(post("/profissionais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profissionalValido())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Registro ja cadastrado."));
    }

    @Test
    void listarTodos_retorna200() throws Exception {
        when(profissionalService.listarTodos()).thenReturn(List.of(profissionalValido()));

        mockMvc.perform(get("/profissionais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void buscarPorId_inexistente_retorna404() throws Exception {
        when(profissionalService.buscarPorId(5L))
                .thenThrow(new RecursoNaoEncontradoException("Profissional nao encontrado com ID: 5"));

        mockMvc.perform(get("/profissionais/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Profissional nao encontrado com ID: 5"));
    }
}
