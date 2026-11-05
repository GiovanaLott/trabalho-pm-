package com.hospital.entidades;

import com.hospital.excecoes.RegraNegocioException;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultas")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "modalidade", discriminatorType = DiscriminatorType.STRING)
public abstract class Consulta extends Atendimento {

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "motivo_consulta", nullable = false, length = 255)
    private String motivoConsulta;

    @Column(name = "observacoes_medicas", columnDefinition = "TEXT")
    private String observacoesMedicas;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false, length = 30)
    private SituacaoConsulta situacao = SituacaoConsulta.AGENDADA;

    protected Consulta() {
        super();
    }

    public Consulta(Long id, Paciente paciente, ProfissionalSaude profissional, LocalDateTime dataHora,
                    String motivoConsulta, String observacoesMedicas, SituacaoConsulta situacao) {
        super(id, paciente, profissional, dataHora, 30);
        this.dataHora = dataHora;
        this.motivoConsulta = motivoConsulta;
        this.observacoesMedicas = observacoesMedicas;
        this.situacao = situacao != null ? situacao : SituacaoConsulta.AGENDADA;
    }

    public Consulta(Long id, Paciente paciente, ProfissionalSaude profissional, LocalDateTime dataHora,
                    String motivoConsulta, String observacoesMedicas, StatusConsulta status) {
        this(id, paciente, profissional, dataHora, motivoConsulta, observacoesMedicas,
                status == StatusConsulta.REALIZADA ? SituacaoConsulta.CONCLUIDA :
                status != null ? SituacaoConsulta.valueOf(status.name()) : SituacaoConsulta.AGENDADA);
    }

    public ModalidadeAtendimento getModalidade() {
        return ModalidadeAtendimento.ROTINA;
    }

    public void validarCondicoesAtendimento() {
        if (getInicioPrevisto() == null && dataHora != null) {
            setInicioPrevisto(dataHora);
        }
        super.validarCondicoesAtendimento();
    }

    public void transitarPara(SituacaoConsulta novaSituacao) {
        if (novaSituacao == null) {
            throw new RegraNegocioException("A nova situação da consulta não pode ser nula.");
        }

        if (this.situacao == novaSituacao) {
            return;
        }

        if (this.situacao == SituacaoConsulta.CONCLUIDA) {
            throw new RegraNegocioException("Uma consulta já concluída não pode ter sua situação alterada.");
        }

        if (this.situacao == SituacaoConsulta.CANCELADA) {
            throw new RegraNegocioException("Uma consulta cancelada não pode ser alterada sem um novo agendamento.");
        }

        if (this.situacao == SituacaoConsulta.NAO_COMPARECEU) {
            throw new RegraNegocioException("Uma consulta finalizada por não comparecimento não pode ter sua situação alterada.");
        }

        switch (novaSituacao) {
            case CONFIRMADA:
                if (this.situacao != SituacaoConsulta.AGENDADA) {
                    throw new RegraNegocioException("Apenas consultas agendadas podem ser confirmadas.");
                }
                break;

            case EM_ATENDIMENTO:
                if (this.situacao != SituacaoConsulta.AGENDADA && this.situacao != SituacaoConsulta.CONFIRMADA) {
                    throw new RegraNegocioException("Para iniciar o atendimento, a consulta deve estar agendada ou confirmada.");
                }
                break;

            case CONCLUIDA:
                if (this.situacao != SituacaoConsulta.EM_ATENDIMENTO) {
                    throw new RegraNegocioException("A consulta só pode ser concluída se estiver em atendimento.");
                }
                break;

            case NAO_COMPARECEU:
                if (this.situacao != SituacaoConsulta.AGENDADA && this.situacao != SituacaoConsulta.CONFIRMADA) {
                    throw new RegraNegocioException("O não comparecimento só pode ser registrado para consultas agendadas ou confirmadas.");
                }
                break;

            case CANCELADA:
                // Permitido cancelar consultas agendadas, confirmadas ou em atendimento
                break;

            case AGENDADA:
                throw new RegraNegocioException("Uma consulta em andamento ou finalizada não pode voltar para o status agendada.");
        }

        this.situacao = novaSituacao;
    }

    public void confirmar() {
        transitarPara(SituacaoConsulta.CONFIRMADA);
    }

    public void iniciarAtendimento() {
        transitarPara(SituacaoConsulta.EM_ATENDIMENTO);
    }

    public void concluir(String observacoes) {
        transitarPara(SituacaoConsulta.CONCLUIDA);
        if (observacoes != null && !observacoes.trim().isEmpty()) {
            this.observacoesMedicas = observacoes;
        }
    }

    public void cancelar() {
        transitarPara(SituacaoConsulta.CANCELADA);
    }

    public void registrarNaoComparecimento() {
        transitarPara(SituacaoConsulta.NAO_COMPARECEU);
    }

    public LocalDateTime getDataHora() {
        return getInicioPrevisto() != null ? getInicioPrevisto() : dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
        setInicioPrevisto(dataHora);
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public String getObservacoesMedicas() {
        return observacoesMedicas;
    }

    public void setObservacoesMedicas(String observacoesMedicas) {
        this.observacoesMedicas = observacoesMedicas;
    }

    public SituacaoConsulta getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoConsulta situacao) {
        this.situacao = situacao;
    }

    public StatusConsulta getStatus() {
        if (situacao == null) return null;
        try {
            return StatusConsulta.valueOf(situacao.name());
        } catch (Exception e) {
            return StatusConsulta.AGENDADA;
        }
    }

    public void setStatus(StatusConsulta status) {
        if (status != null) {
            if (status == StatusConsulta.REALIZADA) {
                this.situacao = SituacaoConsulta.CONCLUIDA;
            } else {
                try {
                    this.situacao = SituacaoConsulta.valueOf(status.name());
                } catch (Exception e) {
                    this.situacao = SituacaoConsulta.AGENDADA;
                }
            }
        }
    }

    @PrePersist
    @PreUpdate
    private void sincronizarDataHoraLegada() {
        if (dataHora == null) {
            dataHora = getInicioPrevisto();
        }
        if (getInicioPrevisto() == null) {
            setInicioPrevisto(dataHora);
        }
    }
}
