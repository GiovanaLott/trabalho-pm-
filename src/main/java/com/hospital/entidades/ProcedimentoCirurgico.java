package com.hospital.entidades;

import com.hospital.excecoes.RegraNegocioException;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "procedimentos_cirurgicos")
@AssociationOverride(name = "profissional",
        joinColumns = @JoinColumn(name = "cirurgiao_id", nullable = false))
public class ProcedimentoCirurgico extends Atendimento {

    @Column(nullable = false, length = 150)
    private String descricao;

    @Column(name = "tipo_procedimento", length = 100)
    private String tipo;

    @Column(name = "data_cirurgia", nullable = false)
    private LocalDate data;

    @Column(name = "horario_inicio_previsto", nullable = false)
    private LocalTime horarioInicioPrevisto;

    @Column(name = "horario_termino")
    private LocalTime horarioTermino;

    @Column(name = "inicio_efetivo")
    private LocalDateTime inicioEfetivo;

    @Column(name = "fim_efetivo")
    private LocalDateTime fimEfetivo;

    @Column(name = "observacoes_pre_operatorias", columnDefinition = "TEXT")
    private String observacoesPreOperatorias;

    @Column(name = "observacoes_pos_operatorias", columnDefinition = "TEXT")
    private String observacoesPosOperatorias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCirurgia situacao = StatusCirurgia.AGENDADA;

    public ProcedimentoCirurgico() {
        super();
    }

    public ProcedimentoCirurgico(Long id, String descricao, Paciente paciente, ProfissionalSaude cirurgiao,
                                LocalDate data, LocalTime horarioInicioPrevisto, LocalTime horarioTermino,
                                String observacoesPreOperatorias, String observacoesPosOperatorias,
                                StatusCirurgia situacao) {
        super(id, paciente, cirurgiao,
                data != null && horarioInicioPrevisto != null
                        ? LocalDateTime.of(data, horarioInicioPrevisto) : null,
                60);
        this.descricao = descricao;
        this.data = data;
        this.horarioInicioPrevisto = horarioInicioPrevisto;
        this.horarioTermino = horarioTermino;
        this.observacoesPreOperatorias = observacoesPreOperatorias;
        this.observacoesPosOperatorias = observacoesPosOperatorias;
        this.situacao = situacao != null ? situacao : StatusCirurgia.AGENDADA;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public void validarCondicoesAtendimento() {
        super.validarCondicoesAtendimento();
        if (descricao == null || descricao.isBlank()) {
            throw new RegraNegocioException("A descrição do procedimento cirúrgico é obrigatória.");
        }
        if (tipo == null || tipo.isBlank()) {
            throw new RegraNegocioException("O tipo do procedimento cirúrgico é obrigatório.");
        }
        if (!(getProfissional() instanceof Cirurgiao)) {
            throw new RegraNegocioException(
                    "O profissional responsável pelo procedimento deve pertencer à categoria cirurgião.");
        }
    }

    public ProfissionalSaude getCirurgiao() {
        return getProfissional();
    }

    public void setCirurgiao(ProfissionalSaude cirurgiao) {
        setProfissional(cirurgiao);
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
        sincronizarInicioPrevisto();
    }

    public LocalTime getHorarioInicioPrevisto() {
        return horarioInicioPrevisto;
    }

    public void setHorarioInicioPrevisto(LocalTime horarioInicioPrevisto) {
        this.horarioInicioPrevisto = horarioInicioPrevisto;
        sincronizarInicioPrevisto();
    }

    public LocalTime getHorarioTermino() {
        return horarioTermino;
    }

    public void setHorarioTermino(LocalTime horarioTermino) {
        this.horarioTermino = horarioTermino;
    }

    public LocalDateTime getInicioEfetivo() {
        return inicioEfetivo;
    }

    public void setInicioEfetivo(LocalDateTime inicioEfetivo) {
        this.inicioEfetivo = inicioEfetivo;
    }

    public LocalDateTime getFimEfetivo() {
        return fimEfetivo;
    }

    public void setFimEfetivo(LocalDateTime fimEfetivo) {
        this.fimEfetivo = fimEfetivo;
        if (fimEfetivo != null) {
            this.horarioTermino = fimEfetivo.toLocalTime();
        }
    }

    public String getObservacoesPreOperatorias() {
        return observacoesPreOperatorias;
    }

    public void setObservacoesPreOperatorias(String observacoesPreOperatorias) {
        this.observacoesPreOperatorias = observacoesPreOperatorias;
    }

    public String getObservacoesPosOperatorias() {
        return observacoesPosOperatorias;
    }

    public void setObservacoesPosOperatorias(String observacoesPosOperatorias) {
        this.observacoesPosOperatorias = observacoesPosOperatorias;
    }

    public StatusCirurgia getSituacao() {
        return situacao;
    }

    public void setSituacao(StatusCirurgia situacao) {
        this.situacao = situacao;
    }

    @PrePersist
    @PreUpdate
    private void sincronizarCamposLegados() {
        if (getInicioPrevisto() != null) {
            data = getInicioPrevisto().toLocalDate();
            horarioInicioPrevisto = getInicioPrevisto().toLocalTime();
        } else {
            sincronizarInicioPrevisto();
        }
    }

    private void sincronizarInicioPrevisto() {
        if (data != null && horarioInicioPrevisto != null) {
            setInicioPrevisto(LocalDateTime.of(data, horarioInicioPrevisto));
        }
    }
}
