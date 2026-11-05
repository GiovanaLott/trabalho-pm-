package com.hospital.entidades;

import com.hospital.excecoes.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class AtendimentoValidacaoTest {

    private final Paciente paciente = new Paciente(1L, "Maria", "529.982.247-25",
            LocalDate.of(1990, 1, 1), null, null, null);
    private final LocalDateTime amanha = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);

    @Test
    void consultaDeRotinaDeveValidarDadosComunsEMotivo() {
        ConsultaRotina consulta = new ConsultaRotina(null, paciente,
                new ClinicoGeral(1L, "Dra. Ana", "CRM-1", null, null),
                amanha, "Acompanhamento", null, SituacaoConsulta.AGENDADA);
        consulta.setDuracaoMinutos(30);

        assertDoesNotThrow(consulta::validarCondicoesAtendimento);

        consulta.setMotivoConsulta(" ");
        assertThrows(RegraNegocioException.class, consulta::validarCondicoesAtendimento);
    }

    @Test
    void consultaEspecializadaDeveExigirUmEspecialistaCompativel() {
        ConsultaEspecializada consulta = new ConsultaEspecializada(null, paciente,
                new Cirurgiao(2L, "Dr. Bruno", "CRM-2", "Cardiologia", null, null),
                amanha, "Avaliação", null, SituacaoConsulta.AGENDADA, "Cardiologia");

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                consulta::validarCondicoesAtendimento);
        assertTrue(erro.getMessage().toLowerCase().contains("especialista"));
    }

    @Test
    void emergenciaTambemDeveValidarInicioEDuracaoComuns() {
        AtendimentoEmergencia emergencia = new AtendimentoEmergencia(null, paciente,
                new ClinicoGeral(3L, "Dra. Carla", "CRM-3", null, null),
                null, "Dor aguda", null, SituacaoConsulta.AGENDADA, PrioridadeAtendimento.ALTA);
        emergencia.setDuracaoMinutos(30);

        assertThrows(RegraNegocioException.class, emergencia::validarCondicoesAtendimento);
    }

    @Test
    void cirurgiaDeveExigirTipoECirurgiaoDaCategoriaCorreta() throws Exception {
        ProcedimentoCirurgico procedimento = new ProcedimentoCirurgico(null, "Apendicectomia", paciente,
                new ClinicoGeral(4L, "Dr. Daniel", "CRM-4", null, null),
                amanha.toLocalDate(), amanha.toLocalTime(), null,
                "Jejum", null, StatusCirurgia.AGENDADA);
        definirTipo(procedimento, "Cirurgia abdominal");

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                procedimento::validarCondicoesAtendimento);
        assertTrue(erro.getMessage().toLowerCase().contains("cirurgião"));
    }

    @Test
    void cirurgiaDeveValidarDuracaoPositiva() throws Exception {
        ProcedimentoCirurgico procedimento = new ProcedimentoCirurgico(null, "Apendicectomia", paciente,
                new Cirurgiao(5L, "Dra. Elisa", "CRM-5", "Cirurgia Geral", null, null),
                amanha.toLocalDate(), amanha.toLocalTime(), null,
                "Jejum", null, StatusCirurgia.AGENDADA);
        definirTipo(procedimento, "Cirurgia abdominal");
        procedimento.setDuracaoMinutos(0);

        assertThrows(RegraNegocioException.class, procedimento::validarCondicoesAtendimento);
    }

    private void definirTipo(ProcedimentoCirurgico procedimento, String tipo) throws Exception {
        Method setter;
        try {
            setter = ProcedimentoCirurgico.class.getMethod("setTipo", String.class);
        } catch (NoSuchMethodException e) {
            fail("O procedimento cirúrgico ainda não possui o campo tipo");
            return;
        }
        setter.invoke(procedimento, tipo);
    }
}
