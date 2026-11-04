package com.hospital.dtos;

import com.hospital.entidades.*;
import java.time.LocalDateTime;

public class ConsultaDTO {

    private Long id;
    private PacienteDTO paciente;
    private ProfissionalSaudeDTO profissional;
    private LocalDateTime dataHora;
    private String motivoConsulta;
    private String observacoesMedicas;
    private SituacaoConsulta situacao;
    private StatusConsulta status;
    private ModalidadeAtendimento modalidade;
    private String especialidade;
    private PrioridadeAtendimento prioridade;
    private Boolean aguardandoAvaliacao;

    public ConsultaDTO() {
    }

    public ConsultaDTO(Consulta consulta) {
        if (consulta != null) {
            this.id = consulta.getId();
            this.paciente = consulta.getPaciente() != null ? new PacienteDTO(consulta.getPaciente()) : null;
            this.profissional = consulta.getProfissional() != null ? new ProfissionalSaudeDTO(consulta.getProfissional()) : null;
            this.dataHora = consulta.getDataHora();
            this.motivoConsulta = consulta.getMotivoConsulta();
            this.observacoesMedicas = consulta.getObservacoesMedicas();
            this.situacao = consulta.getSituacao();
            this.status = consulta.getStatus();
            this.modalidade = consulta.getModalidade();

            if (consulta instanceof ConsultaEspecializada specialized) {
                this.especialidade = specialized.getEspecialidade();
            } else if (consulta instanceof AtendimentoEmergencia emergencia) {
                this.prioridade = emergencia.getPrioridade();
                this.aguardandoAvaliacao = emergencia.isAguardandoAvaliacao();
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PacienteDTO getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteDTO paciente) {
        this.paciente = paciente;
    }

    public ProfissionalSaudeDTO getProfissional() {
        return profissional;
    }

    public void setProfissional(ProfissionalSaudeDTO profissional) {
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
        if (situacao != null) {
            try {
                this.status = StatusConsulta.valueOf(situacao.name());
            } catch (Exception ignored) {
            }
        }
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public void setStatus(StatusConsulta status) {
        this.status = status;
        if (status != null) {
            if (status == StatusConsulta.REALIZADA) {
                this.situacao = SituacaoConsulta.CONCLUIDA;
            } else {
                try {
                    this.situacao = SituacaoConsulta.valueOf(status.name());
                } catch (Exception ignored) {
                }
            }
        }
    }

    public ModalidadeAtendimento getModalidade() {
        return modalidade;
    }

    public void setModalidade(ModalidadeAtendimento modalidade) {
        this.modalidade = modalidade;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public PrioridadeAtendimento getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(PrioridadeAtendimento prioridade) {
        this.prioridade = prioridade;
    }

    public Boolean getAguardandoAvaliacao() {
        return aguardandoAvaliacao;
    }

    public void setAguardandoAvaliacao(Boolean aguardandoAvaliacao) {
        this.aguardandoAvaliacao = aguardandoAvaliacao;
    }
}
