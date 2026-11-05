package com.hospital.entidades;

import com.hospital.excecoes.QuartoIndisponivelException;
import com.hospital.excecoes.QuartoLotadoException;
import com.hospital.excecoes.RegraNegocioException;
import jakarta.persistence.*;
import java.util.Objects;

/**
 * Quarto hospitalar. Classe abstrata: cada tipo de quarto (enfermaria, apartamento, UTI e
 * isolamento) é uma subclasse que define seus limites de capacidade e, quando necessário,
 * requisitos próprios para receber uma internação.
 *
 * <p>Regras garantidas pela própria classe:
 * <ul>
 *   <li>a ocupação nunca ultrapassa a capacidade máxima (regra de negócio 5);</li>
 *   <li>a situação DISPONIVEL/OCUPADO é recalculada a cada entrada ou saída de paciente;</li>
 *   <li>só quartos vazios podem entrar em manutenção, e quartos em manutenção não recebem pacientes;</li>
 *   <li>a capacidade não pode ser reduzida abaixo da ocupação atual.</li>
 * </ul>
 *
 * <p>O campo {@code versao} ativa o controle de concorrência otimista do JPA: se duas internações
 * tentarem ocupar o último leito ao mesmo tempo, apenas uma é gravada.
 */
@Entity
@Table(name = "quartos")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_identificacao", nullable = false, unique = true, length = 20)
    private String numeroIdentificacao;

    @Column(nullable = false)
    private Integer andar;

    @Column(name = "capacidade_maxima", nullable = false)
    private Integer capacidadeMaxima;

    @Column(name = "ocupacao_atual", nullable = false)
    private Integer ocupacaoAtual = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusQuarto situacao = StatusQuarto.DISPONIVEL;

    @Version
    private Long versao;

    protected Quarto() {
    }

    protected Quarto(Long id, String numeroIdentificacao, Integer andar, Integer capacidadeMaxima,
                     Integer ocupacaoAtual, StatusQuarto situacao) {
        this.id = id;
        this.numeroIdentificacao = numeroIdentificacao;
        this.andar = andar;
        this.capacidadeMaxima = capacidadeMaxima;
        this.ocupacaoAtual = ocupacaoAtual != null ? ocupacaoAtual : 0;
        this.situacao = situacao != null ? situacao : StatusQuarto.DISPONIVEL;
    }

    // ------------------------------------------------------------------
    // Comportamento polimórfico
    // ------------------------------------------------------------------

    /** Tipo do quarto (definido por cada subclasse). */
    public abstract TipoQuarto getTipo();

    /**
     * Valida os dados do quarto. Subclasses podem acrescentar verificações próprias
     * sobrescrevendo este método e chamando {@code super.validarConfiguracao()}.
     */
    public void validarConfiguracao() {
        if (numeroIdentificacao == null || numeroIdentificacao.isBlank()) {
            throw new RegraNegocioException("O número de identificação do quarto é obrigatório.");
        }
        if (andar == null || andar < 0) {
            throw new RegraNegocioException("O andar do quarto deve ser informado (0 para o térreo).");
        }
        validarCapacidadeDoTipo(capacidadeMaxima);
        if (ocupacaoAtual > capacidadeMaxima) {
            throw new RegraNegocioException("A ocupação atual não pode ser maior que a capacidade máxima do quarto.");
        }
    }

    /**
     * Verifica se o quarto pode receber a internação informada e, em caso positivo, ocupa um leito.
     * Segue o padrão Template Method: as verificações comuns ficam aqui e cada tipo de quarto pode
     * acrescentar requisitos em {@link #validarRequisitosDoTipo(Internacao)}.
     */
    public final void registrarAdmissao(Internacao internacao) {
        if (situacao == StatusQuarto.EM_MANUTENCAO) {
            throw new QuartoIndisponivelException("O quarto " + numeroIdentificacao
                    + " está em manutenção e não pode receber pacientes.");
        }
        validarRequisitosDoTipo(internacao);
        incrementarOcupacao();
    }

    /** Requisitos específicos de cada tipo de quarto para receber uma internação. */
    protected void validarRequisitosDoTipo(Internacao internacao) {
        // Por padrão, não há requisitos adicionais.
    }

    // ------------------------------------------------------------------
    // Ocupação
    // ------------------------------------------------------------------

    public boolean temVagaDisponivel() {
        return this.situacao == StatusQuarto.DISPONIVEL && this.ocupacaoAtual < this.capacidadeMaxima;
    }

    /** Ocupa um leito. Lança {@link QuartoLotadoException} se não houver vaga. */
    public void incrementarOcupacao() {
        if (!temVagaDisponivel()) {
            throw new QuartoLotadoException("O quarto " + numeroIdentificacao
                    + " não está disponível ou atingiu sua capacidade máxima (" + capacidadeMaxima + " leito(s)).");
        }
        this.ocupacaoAtual++;
        atualizarSituacao();
    }

    /** Libera um leito (alta ou cancelamento de internação). */
    public void decrementarOcupacao() {
        if (this.ocupacaoAtual > 0) {
            this.ocupacaoAtual--;
        }
        atualizarSituacao();
    }

    public int getLeitosDisponiveis() {
        if (situacao == StatusQuarto.EM_MANUTENCAO) {
            return 0;
        }
        return Math.max(0, capacidadeMaxima - ocupacaoAtual);
    }

    /** Recalcula a situação a partir da ocupação (não altera quartos em manutenção). */
    private void atualizarSituacao() {
        if (this.situacao == StatusQuarto.EM_MANUTENCAO) {
            return;
        }
        this.situacao = this.ocupacaoAtual >= this.capacidadeMaxima ? StatusQuarto.OCUPADO : StatusQuarto.DISPONIVEL;
    }

    // ------------------------------------------------------------------
    // Manutenção e alteração de capacidade
    // ------------------------------------------------------------------

    public void colocarEmManutencao() {
        if (situacao == StatusQuarto.EM_MANUTENCAO) {
            throw new RegraNegocioException("O quarto " + numeroIdentificacao + " já está em manutenção.");
        }
        if (ocupacaoAtual > 0) {
            throw new RegraNegocioException("O quarto " + numeroIdentificacao + " possui " + ocupacaoAtual
                    + " paciente(s) internado(s) e não pode entrar em manutenção.");
        }
        this.situacao = StatusQuarto.EM_MANUTENCAO;
    }

    public void liberarDaManutencao() {
        if (situacao != StatusQuarto.EM_MANUTENCAO) {
            throw new RegraNegocioException("O quarto " + numeroIdentificacao + " não está em manutenção.");
        }
        this.situacao = StatusQuarto.DISPONIVEL;
        atualizarSituacao();
    }

    public void alterarCapacidade(Integer novaCapacidade) {
        if (novaCapacidade == null) {
            return;
        }
        if (novaCapacidade < ocupacaoAtual) {
            throw new RegraNegocioException("A capacidade não pode ser reduzida para " + novaCapacidade
                    + " leito(s): o quarto possui " + ocupacaoAtual + " paciente(s) internado(s).");
        }
        validarCapacidadeDoTipo(novaCapacidade);
        this.capacidadeMaxima = novaCapacidade;
        atualizarSituacao();
    }

    private void validarCapacidadeDoTipo(Integer capacidade) {
        TipoQuarto tipo = getTipo();
        if (capacidade == null || capacidade < tipo.getCapacidadeMinima() || capacidade > tipo.getCapacidadeMaxima()) {
            throw new RegraNegocioException(String.format(
                    "Um quarto do tipo %s deve ter entre %d e %d leito(s). Capacidade informada: %s.",
                    tipo.getDescricao(), tipo.getCapacidadeMinima(), tipo.getCapacidadeMaxima(), capacidade));
        }
    }

    // ------------------------------------------------------------------
    // Getters e setters
    // ------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroIdentificacao() {
        return numeroIdentificacao;
    }

    public void setNumeroIdentificacao(String numeroIdentificacao) {
        this.numeroIdentificacao = numeroIdentificacao;
    }

    public Integer getAndar() {
        return andar;
    }

    public void setAndar(Integer andar) {
        this.andar = andar;
    }

    public Integer getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public void setCapacidadeMaxima(Integer capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
    }

    public Integer getOcupacaoAtual() {
        return ocupacaoAtual;
    }

    public void setOcupacaoAtual(Integer ocupacaoAtual) {
        this.ocupacaoAtual = ocupacaoAtual;
    }

    public StatusQuarto getSituacao() {
        return situacao;
    }

    public void setSituacao(StatusQuarto situacao) {
        this.situacao = situacao;
    }

    public Long getVersao() {
        return versao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quarto quarto = (Quarto) o;
        return Objects.equals(id, quarto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
