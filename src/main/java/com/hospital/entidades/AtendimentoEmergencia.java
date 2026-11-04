package com.hospital.entidades;

import com.hospital.excecoes.RegraNegocioException;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("EMERGENCIA")
public class AtendimentoEmergencia extends Consulta {

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade_emergencia", length = 20)
    private PrioridadeAtendimento prioridade;

    public AtendimentoEmergencia() {
        super();
    }

    public AtendimentoEmergencia(Long id, Paciente paciente, ProfissionalSaude profissional,
                                 LocalDateTime dataHora, String motivoConsulta, String observacoesMedicas,
                                 SituacaoConsulta situacao, PrioridadeAtendimento prioridade) {
        super(id, paciente, profissional, dataHora, motivoConsulta, observacoesMedicas, situacao);
        this.prioridade = prioridade;
    }

    @Override
    public ModalidadeAtendimento getModalidade() {
        return ModalidadeAtendimento.EMERGENCIA;
    }

    @Override
    public void validarCondicoesAtendimento() {
        if (getPaciente() == null) {
            throw new RegraNegocioException("Paciente é obrigatório para atendimento de emergência.");
        }
        if (getProfissional() == null) {
            throw new RegraNegocioException("Profissional de saúde é obrigatório para atendimento de emergência.");
        }
        if (this.prioridade == null) {
            throw new RegraNegocioException("A classificação de prioridade é obrigatória para atendimentos de emergência.");
        }
    }

    public boolean isAguardandoAvaliacao() {
        return getSituacao() == SituacaoConsulta.AGENDADA || getSituacao() == SituacaoConsulta.CONFIRMADA;
    }

    public boolean isEmAtendimento() {
        return getSituacao() == SituacaoConsulta.EM_ATENDIMENTO;
    }

    public boolean isConcluido() {
        return getSituacao() == SituacaoConsulta.CONCLUIDA;
    }

    public PrioridadeAtendimento getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(PrioridadeAtendimento prioridade) {
        this.prioridade = prioridade;
    }
}
