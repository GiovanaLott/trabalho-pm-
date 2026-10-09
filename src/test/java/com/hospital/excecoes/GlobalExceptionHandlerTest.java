package com.hospital.excecoes;

import com.hospital.controladores.PacienteController;
import com.hospital.entidades.Quarto;
import com.hospital.servicos.PacienteService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa o GlobalExceptionHandler de forma isolada: usa o PacienteController com o service
 * "mockado" para provocar cada tipo de excecao e confere o codigo HTTP e o corpo da resposta.
 */
@WebMvcTest(PacienteController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PacienteService pacienteService;

    // ---------------------- excecoes de dominio ----------------------

    @Test
    void recursoNaoEncontrado_retorna404ComCorpoPadronizado() throws Exception {
        when(pacienteService.buscarPorId(1L)).thenThrow(new RecursoNaoEncontradoException("Paciente nao encontrado"));

        mockMvc.perform(get("/pacientes/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").value("Paciente nao encontrado"))
                .andExpect(jsonPath("$.path").value("/pacientes/1"))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void regraDeNegocio_retorna400() throws Exception {
        when(pacienteService.buscarPorId(1L)).thenThrow(new RegraNegocioException("Regra violada"));

        mockMvc.perform(get("/pacientes/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Regra violada"));
    }

    @Test
    void choqueDeHorario_retorna409() throws Exception {
        when(pacienteService.buscarPorId(1L)).thenThrow(new ChoqueHorarioException("Horario ocupado"));

        mockMvc.perform(get("/pacientes/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Horario ocupado"));
    }

    @Test
    void quartoLotado_retorna409() throws Exception {
        when(pacienteService.buscarPorId(1L)).thenThrow(new QuartoLotadoException("Quarto cheio"));

        mockMvc.perform(get("/pacientes/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Quarto cheio"));
    }

    // ---------------------- persistencia e concorrencia ----------------------

    @Test
    void violacaoDeIntegridade_retorna409SemExporDetalhesDoBanco() throws Exception {
        when(pacienteService.buscarPorId(1L)).thenThrow(
                new DataIntegrityViolationException("could not execute statement; constraint [uk_cpf] tabela pacientes"));

        mockMvc.perform(get("/pacientes/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(content().string(not(containsString("uk_cpf"))))
                .andExpect(content().string(not(containsString("could not execute statement"))));
    }

    @Test
    void conflitoDeLockOtimista_retorna409() throws Exception {
        when(pacienteService.buscarPorId(1L)).thenThrow(new ObjectOptimisticLockingFailureException(Quarto.class, 1L));

        mockMvc.perform(get("/pacientes/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }

    // ---------------------- validacao ----------------------

    @Test
    void constraintViolation_retorna400() throws Exception {
        when(pacienteService.buscarPorId(1L)).thenThrow(new ConstraintViolationException("invalido", new HashSet<>()));

        mockMvc.perform(get("/pacientes/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void beanValidation_retornaMapaComUmaMensagemPorCampo() throws Exception {
        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\" \",\"cpf\":\"\",\"email\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/pacientes"))
                .andExpect(jsonPath("$.errors.nome").exists())
                .andExpect(jsonPath("$.errors.cpf").exists())
                .andExpect(jsonPath("$.errors.dataNascimento").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    // ---------------------- erros do Spring MVC ----------------------

    @Test
    void jsonMalformado_retorna400() throws Exception {
        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ nome: sem aspas"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void corpoAusente_retorna400() throws Exception {
        mockMvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void idComTipoErrado_retorna400CitandoOValorEnviado() throws Exception {
        mockMvc.perform(get("/pacientes/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("abc")));
    }

    @Test
    void rotaInexistente_retorna404NoFormatoPadrao() throws Exception {
        mockMvc.perform(get("/rota-que-nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/rota-que-nao-existe"));
    }

    @Test
    void metodoNaoPermitido_retorna405() throws Exception {
        mockMvc.perform(delete("/pacientes/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void tipoDeMidiaNaoSuportado_retorna415() throws Exception {
        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("nome=Joao"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    // ---------------------- rede de seguranca ----------------------

    @Test
    void erroInesperado_retorna500SemVazarAMensagemInterna() throws Exception {
        when(pacienteService.buscarPorId(1L)).thenThrow(new IllegalStateException("senha do banco: segredo123"));

        mockMvc.perform(get("/pacientes/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado. Tente novamente mais tarde."))
                .andExpect(content().string(not(containsString("segredo123"))))
                .andExpect(content().string(not(containsString("IllegalStateException"))));
    }
}
