package com.hospital.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.entidades.Enfermaria;
import com.hospital.entidades.Quarto;
import com.hospital.repositorios.QuartoRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração: sobem a aplicação inteira (Spring + JPA + banco H2 em memória) e
 * exercitam a API REST de ponta a ponta, passando por controllers, services, repositórios e banco.
 * Também garantem que a aplicação consegue iniciar (mapeamentos JPA e queries válidos).
 */
@SpringBootTest
@AutoConfigureMockMvc
class HospitalApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuartoRepository quartoRepository;

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private ResultActions enviar(String metodo, String url, Object corpo) throws Exception {
        var requisicao = switch (metodo) {
            case "POST" -> post(url);
            case "PUT" -> put(url);
            default -> throw new IllegalArgumentException(metodo);
        };
        if (corpo != null) {
            requisicao.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(corpo));
        }
        return mockMvc.perform(requisicao);
    }

    private long idDe(MvcResult resultado) throws Exception {
        return ((Number) JsonPath.read(resultado.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private long criarPaciente(String nome, String cpf) throws Exception {
        Map<String, Object> paciente = new LinkedHashMap<>();
        paciente.put("nome", nome);
        paciente.put("cpf", cpf);
        paciente.put("dataNascimento", "1980-04-12");
        paciente.put("telefone", "(31) 98888-7777");
        paciente.put("email", "paciente@email.com");
        return idDe(enviar("POST", "/pacientes", paciente).andExpect(status().isCreated()).andReturn());
    }

    private long criarProfissional(String registro, String especialidade) throws Exception {
        Map<String, Object> profissional = Map.of(
                "nome", "Dr. Teste " + registro,
                "registroProfissional", registro,
                "categoria", "ESPECIALISTA",
                "especialidade", especialidade);
        return idDe(enviar("POST", "/profissionais", profissional).andExpect(status().isCreated()).andReturn());
    }

    private long criarQuarto(String numero, String tipo, int capacidade) throws Exception {
        Map<String, Object> quarto = Map.of("numeroIdentificacao", numero, "andar", 2,
                "capacidadeMaxima", capacidade, "tipo", tipo);
        return idDe(enviar("POST", "/quartos", quarto).andExpect(status().isCreated()).andReturn());
    }

    private ResultActions internar(long pacienteId, long profissionalId, long quartoId, String observacoes) throws Exception {
        Map<String, Object> internacao = new LinkedHashMap<>();
        internacao.put("pacienteId", pacienteId);
        internacao.put("profissionalId", profissionalId);
        internacao.put("quartoId", quartoId);
        internacao.put("dataPrevistaAlta", LocalDate.now().plusDays(7).toString());
        internacao.put("observacoes", observacoes);
        return enviar("POST", "/internacoes", internacao);
    }

    // ------------------------------------------------------------------
    // Cenários
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Fluxo completo: paciente, quarto UTI, internação, lotação, histórico e alta")
    void fluxoCompletoDeInternacaoEmUti() throws Exception {
        long pacienteId = criarPaciente("Helena Prado", "39053344705");
        mockMvc.perform(get("/pacientes/{id}", pacienteId))
                .andExpect(jsonPath("$.cpf").value("390.533.447-05"))
                .andExpect(jsonPath("$.idade").isNumber());

        // CPF repetido (agora com máscara) não pode ser cadastrado de novo
        enviar("POST", "/pacientes", Map.of("nome", "Outra", "cpf", "390.533.447-05", "dataNascimento", "1990-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Já existe")));
        mockMvc.perform(get("/pacientes/cpf/39053344705")).andExpect(jsonPath("$.id").value(pacienteId));

        long medicoId = criarProfissional("CRM-IT-1", "Medicina Intensiva");
        long utiId = criarQuarto("UTI-IT-1", "UTI", 1);

        // UTI exige justificativa clínica
        internar(pacienteId, medicoId, utiId, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("justificativa")));
        mockMvc.perform(get("/quartos/{id}", utiId)).andExpect(jsonPath("$.ocupacaoAtual").value(0));

        long internacaoId = idDe(internar(pacienteId, medicoId, utiId, "Choque séptico")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quarto.tipo").value("UTI"))
                .andReturn());

        mockMvc.perform(get("/quartos/{id}", utiId))
                .andExpect(jsonPath("$.situacao").value("OCUPADO"))
                .andExpect(jsonPath("$.leitosDisponiveis").value(0));
        mockMvc.perform(get("/quartos/{id}/internacoes", utiId))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].paciente.nome").value("Helena Prado"));

        // Regra 5: quarto lotado não recebe outro paciente (409)
        long outroPacienteId = criarPaciente("Igor Matos", "111.444.777-35");
        internar(outroPacienteId, medicoId, utiId, "Pós-operatório")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflito de regra de negócio"));

        // Quarto ocupado não entra em manutenção; paciente e quarto com histórico não são excluídos
        mockMvc.perform(put("/quartos/{id}/manutencao", utiId)).andExpect(status().isBadRequest());
        mockMvc.perform(delete("/pacientes/{id}", pacienteId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("histórico")));
        mockMvc.perform(delete("/quartos/{id}", utiId)).andExpect(status().isBadRequest());

        mockMvc.perform(get("/historico/paciente/{id}", pacienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.internacoes.length()").value(1))
                .andExpect(jsonPath("$.procedimentosCirurgicos").isArray());

        // Alta libera o leito
        mockMvc.perform(put("/internacoes/{id}/alta", internacaoId)).andExpect(status().isOk());
        mockMvc.perform(get("/quartos/{id}", utiId))
                .andExpect(jsonPath("$.situacao").value("DISPONIVEL"))
                .andExpect(jsonPath("$.leitosDisponiveis").value(1));
    }

    @Test
    @DisplayName("Paciente sem histórico pode ser atualizado e excluído")
    void pacienteSemHistoricoPodeSerAtualizadoEExcluido() throws Exception {
        long id = criarPaciente("Joana Reis", "935.411.347-80");

        Map<String, Object> alteracao = new LinkedHashMap<>();
        alteracao.put("nome", "Joana Reis Alves");
        alteracao.put("cpf", "93541134780");
        alteracao.put("dataNascimento", "1980-04-12");
        alteracao.put("endereco", "Av. Afonso Pena, 1000");
        enviar("PUT", "/pacientes/" + id, alteracao)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joana Reis Alves"))
                .andExpect(jsonPath("$.endereco").value("Av. Afonso Pena, 1000"));

        mockMvc.perform(get("/pacientes").param("nome", "reis alves"))
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(delete("/pacientes/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/pacientes/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Quarto em manutenção não recebe internação e pode ser excluído se nunca foi usado")
    void quartoEmManutencao() throws Exception {
        long quartoId = criarQuarto("ENF-IT-1", "ENFERMARIA", 4);
        long pacienteId = criarPaciente("Lucas Prado", "123.456.789-09");
        long medicoId = criarProfissional("CRM-IT-2", "Clínica Geral");

        mockMvc.perform(put("/quartos/{id}/manutencao", quartoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("EM_MANUTENCAO"));
        mockMvc.perform(get("/quartos/disponiveis").param("tipo", "ENFERMARIA"))
                .andExpect(jsonPath("$[?(@.id == " + quartoId + ")]").isEmpty());

        internar(pacienteId, medicoId, quartoId, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("manutenção")));

        mockMvc.perform(put("/quartos/{id}/liberar", quartoId))
                .andExpect(jsonPath("$.situacao").value("DISPONIVEL"));
        mockMvc.perform(get("/quartos/ocupacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porTipo.length()").value(4));

        mockMvc.perform(delete("/quartos/{id}", quartoId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/quartos/{id}", quartoId)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Capacidade fora da faixa do tipo é rejeitada")
    void capacidadeForaDoTipo() throws Exception {
        enviar("POST", "/quartos", Map.of("numeroIdentificacao", "AP-IT-9", "andar", 1, "capacidadeMaxima", 3, "tipo", "APARTAMENTO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("entre 1 e 2")));
    }

    @Test
    @DisplayName("Consultas com herança (rotina/especializada) funcionam no banco e respeitam o choque de horário")
    void consultasNoBanco() throws Exception {
        long pacienteId = criarPaciente("Marta Dias", "111.222.333-96");
        long cardiologistaId = criarProfissional("CRM-IT-3", "Cardiologia");
        LocalDateTime horario = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0);

        Map<String, Object> consulta = new LinkedHashMap<>();
        consulta.put("pacienteId", pacienteId);
        consulta.put("profissionalId", cardiologistaId);
        consulta.put("dataHora", horario.toString());
        consulta.put("motivoConsulta", "Dor no peito");
        consulta.put("modalidade", "ESPECIALIZADA");
        consulta.put("especialidade", "Cardiologia");

        enviar("POST", "/consultas", consulta)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.modalidade").value("ESPECIALIZADA"));

        consulta.put("dataHora", horario.plusMinutes(15).toString());
        enviar("POST", "/consultas", consulta).andExpect(status().isConflict());

        mockMvc.perform(get("/consultas/modalidade/ESPECIALIZADA"))
                .andExpect(jsonPath("$[?(@.paciente.id == " + pacienteId + ")]").isNotEmpty());
        mockMvc.perform(get("/historico/paciente/{id}", pacienteId))
                .andExpect(jsonPath("$.consultas.length()").value(1));
    }

    @Test
    @DisplayName("Erros de requisição viram respostas JSON padronizadas (sem erro 500)")
    void errosDeRequisicao() throws Exception {
        mockMvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON).content("{ json quebrado"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Requisição inválida"));
        mockMvc.perform(get("/consultas/situacao/INEXISTENTE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parâmetro inválido"));
        mockMvc.perform(get("/pacientes/abc")).andExpect(status().isBadRequest());
        mockMvc.perform(delete("/consultas")).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(get("/pacientes/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/pacientes/999999"));
    }

    @Test
    @DisplayName("Duas internações simultâneas no último leito: apenas uma é gravada (controle de versão)")
    void concorrenciaNoUltimoLeito() {
        Quarto salvo = quartoRepository.save(new Enfermaria(null, "ENF-IT-CONC", 2, 2, 1, null));

        // Duas requisições leem o mesmo quarto ao mesmo tempo (cópias independentes)
        Quarto leituraA = quartoRepository.findById(salvo.getId()).orElseThrow();
        Quarto leituraB = quartoRepository.findById(salvo.getId()).orElseThrow();

        leituraA.incrementarOcupacao();
        quartoRepository.save(leituraA);

        leituraB.incrementarOcupacao();  // em memória ainda parece haver vaga...
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> quartoRepository.save(leituraB));
    }
}
