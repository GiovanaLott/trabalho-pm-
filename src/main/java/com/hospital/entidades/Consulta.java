package com.hospital.entidades;

import com.hospital.excecoes.RegraNegocioException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "consultas")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "modalidade", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("ROTINA")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "profissional_id", nullable = false)
    private ProfissionalSaude profissional;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "motivo_consulta", nullable = false, length = 255)
    private String motivoConsulta;

    @Column(name = "observacoes_medicas", columnDefinition = "TEXT")
    private String observacoesMedicas;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false, length = 30)
    private SituacaoConsulta situacao = SituacaoConsulta.AGENDADA;

    public Consulta() {
    }

    public Consulta(Long id, Paciente paciente, ProfissionalSaude profissional, LocalDateTime dataHora,
                    String motivoConsulta, String observacoesMedicas, SituacaoConsulta situacao) {
        this.id = id;
        this.paciente = paciente;
        this.profissional = profissional;
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
        if (this.paciente == null) {
            throw new RegraNegocioException("Paciente é obrigatório para o atendimento.");
        }
        if (this.profissional == null) {
            throw new RegraNegocioException("Profissional de saúde é obrigatório para o atendimento.");
        }
        if (this.dataHora == null) {
            throw new RegraNegocioException("Data e hora são obrigatórias.");
        }
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public ProfissionalSaude getProfissional() {
        return profissional;
    }

    public void setProfissional(ProfissionalSaude profissional) {
        this.profissional = profissional;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Consulta consulta = (Consulta) o;
        return Objects.equals(id, consulta.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
