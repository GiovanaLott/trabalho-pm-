package com.hospital.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dtos.ConsultaRequestDTO;
import com.hospital.dtos.InternacaoRequestDTO;
import com.hospital.dtos.PacienteDTO;
import com.hospital.dtos.ProfissionalSaudeDTO;
import com.hospital.dtos.QuartoDTO;
import com.hospital.entidades.Quarto;
import com.hospital.entidades.StatusQuarto;
import com.hospital.repositorios.ConsultaRepository;
import com.hospital.repositorios.InternacaoRepository;
import com.hospital.repositorios.PacienteRepository;
import com.hospital.repositorios.ProfissionalSaudeRepository;
import com.hospital.repositorios.QuartoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de ponta a ponta: sobem a aplicacao inteira (controllers + services + repositories + JPA)
 * com banco H2 em memoria (perfil "h2") e chamam a API REST como um cliente real faria.
 * Cobrem os fluxos principais e as regras de negocio que dependem do banco.
 */
@SpringBootTest(properties = "spring.jpa.show-sql=false")
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class HospitalApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private InternacaoRepository internacaoRepository;

    @Autowired
    private QuartoRepository quartoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalSaudeRepository profissionalRepository;

    @BeforeEach
    void limparBanco() {
        // ordem importa: primeiro quem tem chave estrangeira
        consultaRepository.deleteAll();
        internacaoRepository.deleteAll();
        quartoRepository.deleteAll();
        pacienteRepository.deleteAll();
        profissionalRepository.deleteAll();
    }

    // =====================================================================
    // Consultas: agendamento, choque de horario, cancelamento e historico
    // =====================================================================

    @Test
    void fluxoDeConsultas_respeitaJanelaDeConflitoDoProfissionalECancelamento() throws Exception {
        LocalDateTime t = proximoHorario();
        long paciente = criarPaciente("Ana Souza", "111.111.111-11");
        long medico1 = criarProfissional("Dr. Carlos", "CRM-1");
        long medico2 = criarProfissional("Dra. Lucia", "CRM-2");

        // 1) primeiro agendamento
        long c1 = idDe(agendar(paciente, medico1, t)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADA"))
                .andReturn());

        // 2) mesmo medico, mesmo horario -> 409
        agendar(paciente, medico1, t)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        // 3) mesmo medico, 10 minutos depois (dentro da janela) -> 409
        agendar(paciente, medico1, t.plusMinutes(10)).andExpect(status().isConflict());

        // 4) mesmo medico, 30 minutos depois (fora da janela) -> 201
        agendar(paciente, medico1, t.plusMinutes(30)).andExpect(status().isCreated());

        // 5) outro medico no mesmo horario -> 201
        agendar(paciente, medico2, t).andExpect(status().isCreated());

        // 6) cancelar a primeira consulta; cancelar de novo -> 400
        mockMvc.perform(put("/consultas/" + c1 + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
        mockMvc.perform(put("/consultas/" + c1 + "/cancelar"))
                .andExpect(status().isBadRequest());

        // 7) horario liberado pelo cancelamento pode ser reutilizado
        agendar(paciente, medico1, t).andExpect(status().isCreated());

        // 8) consultas por paciente e por profissional
        mockMvc.perform(get("/consultas/paciente/" + paciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
        mockMvc.perform(get("/consultas/profissional/" + medico1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        // 9) o historico medico reune as consultas do paciente
        mockMvc.perform(get("/historico/paciente/" + paciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paciente.id").value(paciente))
                .andExpect(jsonPath("$.consultas", hasSize(4)))
                .andExpect(jsonPath("$.internacoes", hasSize(0)));
    }

    @Test
    void finalizarConsulta_registraObservacoesEImpedeFinalizarCancelada() throws Exception {
        long paciente = criarPaciente("Bruno Lima", "222.222.222-22");
        long medico = criarProfissional("Dr. Paulo", "CRM-3");
        LocalDateTime t = proximoHorario();

        long realizada = idDe(agendar(paciente, medico, t).andExpect(status().isCreated()).andReturn());
        long cancelada = idDe(agendar(paciente, medico, t.plusHours(2)).andExpect(status().isCreated()).andReturn());

        mockMvc.perform(put("/consultas/" + realizada + "/finalizar").param("observacoesMedicas", "Paciente estavel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REALIZADA"))
                .andExpect(jsonPath("$.observacoesMedicas").value("Paciente estavel"));

        mockMvc.perform(put("/consultas/" + cancelada + "/cancelar")).andExpect(status().isOk());
        mockMvc.perform(put("/consultas/" + cancelada + "/finalizar")).andExpect(status().isBadRequest());
    }

    @Test
    void agendarConsulta_comDataNoPassadoOuIdsInexistentes_retornaErrosAdequados() throws Exception {
        long paciente = criarPaciente("Carla Dias", "333.333.333-33");
        long medico = criarProfissional("Dra. Sonia", "CRM-4");

        agendar(paciente, medico, LocalDateTime.now().minusDays(1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        agendar(999_999L, medico, proximoHorario())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        agendar(paciente, 999_999L, proximoHorario())
                .andExpect(status().isNotFound());
    }

    // =====================================================================
    // Internacoes: capacidade do quarto, alta e liberacao do leito
    // =====================================================================

    @Test
    void fluxoDeInternacao_controlaCapacidadeDoQuartoEliberaLeitoNaAlta() throws Exception {
        long pacienteA = criarPaciente("Paciente A", "444.444.444-44");
        long pacienteB = criarPaciente("Paciente B", "555.555.555-55");
        long medico = criarProfissional("Dr. Responsavel", "CRM-5");
        long quarto = criarQuarto("INT-1", 1, 1);

        // quarto novo aparece como disponivel
        mockMvc.perform(get("/quartos/disponiveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 1) internar A -> 201, quarto fica OCUPADO
        long internacaoA = idDe(internar(pacienteA, medico, quarto)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andReturn());
        mockMvc.perform(get("/quartos/" + quarto))
                .andExpect(jsonPath("$.situacao").value("OCUPADO"))
                .andExpect(jsonPath("$.ocupacaoAtual").value(1));
        mockMvc.perform(get("/quartos/disponiveis"))
                .andExpect(jsonPath("$", hasSize(0)));

        // 2) B no quarto lotado -> 409
        internar(pacienteB, medico, quarto)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        // 3) A de novo (ja internado) -> 400
        internar(pacienteA, medico, quarto)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        // 4) alta de A -> ALTA, quarto volta a DISPONIVEL
        mockMvc.perform(put("/internacoes/" + internacaoA + "/alta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ALTA"))
                .andExpect(jsonPath("$.dataEfetivaAlta").exists());
        mockMvc.perform(get("/quartos/" + quarto))
                .andExpect(jsonPath("$.situacao").value("DISPONIVEL"))
                .andExpect(jsonPath("$.ocupacaoAtual").value(0));

        // 5) alta repetida -> 400
        mockMvc.perform(put("/internacoes/" + internacaoA + "/alta"))
                .andExpect(status().isBadRequest());

        // 6) agora B consegue o leito
        internar(pacienteB, medico, quarto).andExpect(status().isCreated());
        mockMvc.perform(get("/internacoes/em-andamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 7) historico de A tem a internacao encerrada
        mockMvc.perform(get("/historico/paciente/" + pacienteA))
                .andExpect(jsonPath("$.internacoes", hasSize(1)))
                .andExpect(jsonPath("$.internacoes[0].status").value("ALTA"));
    }

    @Test
    void quartoComDoisLeitos_aceitaDoisPacientesEBloqueiaOTerceiro() throws Exception {
        long medico = criarProfissional("Dr. Chefe", "CRM-6");
        long quarto = criarQuarto("INT-2", 2, 2);
        long p1 = criarPaciente("Pessoa 1", "666.666.666-01");
        long p2 = criarPaciente("Pessoa 2", "666.666.666-02");
        long p3 = criarPaciente("Pessoa 3", "666.666.666-03");

        internar(p1, medico, quarto).andExpect(status().isCreated());
        mockMvc.perform(get("/quartos/" + quarto))
                .andExpect(jsonPath("$.situacao").value("DISPONIVEL"))
                .andExpect(jsonPath("$.ocupacaoAtual").value(1));

        internar(p2, medico, quarto).andExpect(status().isCreated());
        mockMvc.perform(get("/quartos/" + quarto))
                .andExpect(jsonPath("$.situacao").value("OCUPADO"));

        internar(p3, medico, quarto).andExpect(status().isConflict());
    }

    @Test
    void internar_comIdsInexistentes_retorna404() throws Exception {
        long paciente = criarPaciente("Paciente X", "777.777.777-77");
        long medico = criarProfissional("Dr. Y", "CRM-7");
        long quarto = criarQuarto("INT-3", 1, 1);

        internar(999_999L, medico, quarto).andExpect(status().isNotFound());
        internar(paciente, 999_999L, quarto).andExpect(status().isNotFound());
        internar(paciente, medico, 999_999L).andExpect(status().isNotFound());
    }

    // =====================================================================
    // Cadastros, validacao e tratamento de erros em producao
    // =====================================================================

    @Test
    void cadastroDePaciente_rejeitaCpfDuplicadoECamposInvalidos() throws Exception {
        criarPaciente("Maria", "888.888.888-88");

        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(novoPaciente("Outra Maria", "888.888.888-88"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"cpf\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nome").exists())
                .andExpect(jsonPath("$.errors.cpf").exists())
                .andExpect(jsonPath("$.errors.dataNascimento").exists());

        assertThat(pacienteRepository.count()).isEqualTo(1);
    }

    @Test
    void cadastroDeProfissionalEQuarto_rejeitaDuplicidade() throws Exception {
        criarProfissional("Dr. Unico", "CRM-9");
        criarQuarto("DUP-1", 1, 1);

        mockMvc.perform(post("/profissionais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(novoProfissional("Dr. Clone", "CRM-9"))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/quartos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(novoQuarto("DUP-1", 1, 1))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/quartos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(novoQuarto("ZERO", 1, 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.capacidadeMaxima").exists());
    }

    @Test
    void erros_vemNoFormatoPadronizadoComCaminhoDaRequisicao() throws Exception {
        mockMvc.perform(get("/pacientes/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/pacientes/999999"))
                .andExpect(jsonPath("$.timestamp").exists());

        mockMvc.perform(get("/pacientes/abc"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/rota/que/nao/existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteId\":1,\"profissionalId\":1,\"dataHora\":\"amanha\",\"motivoConsulta\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // =====================================================================
    // Persistencia: controle de concorrencia otimista (@Version)
    // =====================================================================

    @Test
    void atualizacaoConcorrenteDoMesmoQuarto_falhaPorLockOtimista() {
        Quarto salvo = quartoRepository.save(new Quarto(null, "LOCK-1", 1, 2, 0, StatusQuarto.DISPONIVEL));
        assertThat(salvo.getVersion()).isEqualTo(0L);

        // duas "telas" carregam o mesmo quarto ao mesmo tempo (copias desanexadas)
        Quarto copiaA = quartoRepository.findById(salvo.getId()).orElseThrow();
        Quarto copiaB = quartoRepository.findById(salvo.getId()).orElseThrow();

        copiaA.incrementarOcupacao();
        quartoRepository.save(copiaA);   // a primeira a gravar vence (versao 0 -> 1)

        copiaB.incrementarOcupacao();    // a segunda parte de uma versao ja desatualizada
        assertThatThrownBy(() -> quartoRepository.save(copiaB))
                .isInstanceOf(OptimisticLockingFailureException.class);

        Quarto noBanco = quartoRepository.findById(salvo.getId()).orElseThrow();
        assertThat(noBanco.getOcupacaoAtual()).isEqualTo(1);
        assertThat(noBanco.getVersion()).isEqualTo(1L);
    }

    // =====================================================================
    // Auxiliares
    // =====================================================================

    /** Daqui a 30 dias, as 10:00 em ponto (sempre no futuro e sem milissegundos). */
    private LocalDateTime proximoHorario() {
        return LocalDateTime.now().plusDays(30).withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    private String json(Object objeto) throws Exception {
        return objectMapper.writeValueAsString(objeto);
    }

    private long idDe(MvcResult resultado) throws Exception {
        String corpo = resultado.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(corpo).get("id").asLong();
    }

    private PacienteDTO novoPaciente(String nome, String cpf) {
        PacienteDTO dto = new PacienteDTO();
        dto.setNome(nome);
        dto.setCpf(cpf);
        dto.setDataNascimento(LocalDate.of(1990, 1, 15));
        dto.setTelefone("31999990000");
        dto.setEndereco("Rua das Flores, 10");
        dto.setEmail("contato@email.com");
        return dto;
    }

    private ProfissionalSaudeDTO novoProfissional(String nome, String registro) {
        ProfissionalSaudeDTO dto = new ProfissionalSaudeDTO();
        dto.setNome(nome);
        dto.setRegistroProfissional(registro);
        dto.setEspecialidade("Clinica Geral");
        dto.setTelefone("31988880000");
        dto.setEmail("medico@hospital.com");
        return dto;
    }

    private QuartoDTO novoQuarto(String numero, int andar, int capacidade) {
        QuartoDTO dto = new QuartoDTO();
        dto.setNumeroIdentificacao(numero);
        dto.setAndar(andar);
        dto.setCapacidadeMaxima(capacidade);
        return dto;
    }

    private long criarPaciente(String nome, String cpf) throws Exception {
        return idDe(mockMvc.perform(post("/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(novoPaciente(nome, cpf))))
                .andExpect(status().isCreated())
                .andReturn());
    }

    private long criarProfissional(String nome, String registro) throws Exception {
        return idDe(mockMvc.perform(post("/profissionais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(novoProfissional(nome, registro))))
                .andExpect(status().isCreated())
                .andReturn());
    }

    private long criarQuarto(String numero, int andar, int capacidade) throws Exception {
        return idDe(mockMvc.perform(post("/quartos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(novoQuarto(numero, andar, capacidade))))
                .andExpect(status().isCreated())
                .andReturn());
    }

    private ResultActions agendar(long pacienteId, long profissionalId, LocalDateTime quando) throws Exception {
        ConsultaRequestDTO dto = new ConsultaRequestDTO(pacienteId, profissionalId, quando, "Consulta de rotina", null);
        return mockMvc.perform(post("/consultas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(dto)));
    }

    private ResultActions internar(long pacienteId, long profissionalId, long quartoId) throws Exception {
        InternacaoRequestDTO dto = new InternacaoRequestDTO(pacienteId, profissionalId, quartoId, null, null, "Observacao");
        return mockMvc.perform(post("/internacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(dto)));
    }
}
