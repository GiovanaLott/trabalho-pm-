package com.hospital.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.ProfissionalSaudeDTO;
import com.hospital.entidades.CategoriaProfissional;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    @DisplayName("Deve exigir e devolver a categoria profissional informada")
    void deveCadastrarCategoriaExplicitamente() throws Exception {
        String json = """
                {
                  "nome": "Dra. Helena",
                  "registroProfissional": "CRM-MG-55555",
                  "categoria": "ESPECIALISTA",
                  "especialidade": "Cardiologia",
                  "telefone": "31999990000",
                  "email": "helena@hospital.com"
                }
                """;

        when(profissionalSaudeService.cadastrar(any(ProfissionalSaudeDTO.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        mockMvc.perform(post("/profissionais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoria").value("ESPECIALISTA"));
    }

    @Test
    @DisplayName("Deve cadastrar profissional com sucesso e retornar 201 Created")
    void deveCadastrarProfissionalComSucesso() throws Exception {
        ProfissionalSaudeDTO dto = new ProfissionalSaudeDTO();
        dto.setNome("Dr. Lucas");
        dto.setRegistroProfissional("CRM-MG-12345");
        dto.setCategoria(CategoriaProfissional.ESPECIALISTA);
        dto.setEspecialidade("Cardiologia");
        dto.setEmail("lucas@hospital.com");
        dto.setTelefone("31988881111");

        ProfissionalSaudeDTO salvo = new ProfissionalSaudeDTO();
        salvo.setId(1L);
        salvo.setNome("Dr. Lucas");
        salvo.setRegistroProfissional("CRM-MG-12345");
        salvo.setCategoria(CategoriaProfissional.ESPECIALISTA);
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
        p.setCategoria(CategoriaProfissional.ESPECIALISTA);
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

    @Test
    @DisplayName("Deve pesquisar especialistas por especialidade")
    void devePesquisarPorEspecialidade() throws Exception {
        mockMvc.perform(get("/profissionais/especialidade/Cardiologia"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve atualizar profissional")
    void deveAtualizarProfissional() throws Exception {
        String json = """
                {
                  "nome": "Dra. Helena Atualizada",
                  "registroProfissional": "CRM-MG-55555",
                  "categoria": "ESPECIALISTA",
                  "especialidade": "Cardiologia",
                  "telefone": "31999990001",
                  "email": "helena@hospital.com"
                }
                """;

        mockMvc.perform(put("/profissionais/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve excluir profissional sem vinculos")
    void deveExcluirProfissional() throws Exception {
        mockMvc.perform(delete("/profissionais/1"))
                .andExpect(status().isNoContent());
    }
}
