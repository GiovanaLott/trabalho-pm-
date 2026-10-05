package com.hospital.entidades;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Representa a entidade e modelo de domínio de um Quarto hospitalar.
 * Em conformidade com os requisitos mínimos:
 * - Número de identificação
 * - Andar
 * - Capacidade máxima de pacientes
 * - Situação atual (disponível ou ocupado)
 *
 * @author Guilherme Oliveira
 */
@Entity
@Table(name = "quartos")
public class Quarto {

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

    /**
     * Construtor padrão (obrigatório para JPA).
     */
    public Quarto() {
    }

    /**
     * Construtor completo com ID.
     */
    public Quarto(Long id, String numeroIdentificacao, Integer andar, Integer capacidadeMaxima, Integer ocupacaoAtual, StatusQuarto situacao) {
        this.id = id;
        this.numeroIdentificacao = numeroIdentificacao;
        this.andar = andar;
        this.capacidadeMaxima = capacidadeMaxima;
        this.ocupacaoAtual = ocupacaoAtual != null ? ocupacaoAtual : 0;
        this.situacao = situacao != null ? situacao : StatusQuarto.DISPONIVEL;
        atualizarSituacao();
    }

    /**
     * Construtor sem ID com capacidade e andar.
     */
    public Quarto(String numeroIdentificacao, Integer andar, Integer capacidadeMaxima) {
        this(null, numeroIdentificacao, andar, capacidadeMaxima, 0, StatusQuarto.DISPONIVEL);
    }

    // --- Métodos de Domínio (Java puro / POO / Regras de Negócio) ---

    /**
     * Verifica se o quarto possui leito/vaga disponível para novas internações.
     *
     * @return true se a ocupação atual for menor que a capacidade máxima, false caso contrário.
     */
    public boolean temVagaDisponivel() {
        if (this.capacidadeMaxima == null) return false;
        int ocupados = (this.ocupacaoAtual != null) ? this.ocupacaoAtual : 0;
        return ocupados < this.capacidadeMaxima;
    }

    /**
     * Retorna a quantidade de vagas ainda disponíveis no quarto.
     *
     * @return Número de leitos livres.
     */
    public int getVagasDisponiveis() {
        if (this.capacidadeMaxima == null) return 0;
        int ocupados = (this.ocupacaoAtual != null) ? this.ocupacaoAtual : 0;
        return Math.max(0, this.capacidadeMaxima - ocupados);
    }

    /**
     * Incrementa a contagem de ocupação do quarto e atualiza sua situação.
     */
    public void incrementarOcupacao() {
        if (this.ocupacaoAtual == null) {
            this.ocupacaoAtual = 0;
        }
        this.ocupacaoAtual++;
        atualizarSituacao();
    }

    /**
     * Decrementa a contagem de ocupação do quarto e atualiza sua situação.
     */
    public void decrementarOcupacao() {
        if (this.ocupacaoAtual != null && this.ocupacaoAtual > 0) {
            this.ocupacaoAtual--;
        }
        atualizarSituacao();
    }

    /**
     * Sincroniza o status da situação (DISPONIVEL / OCUPADO) de acordo com a capacidade.
     */
    public void atualizarSituacao() {
        if (this.capacidadeMaxima != null && this.ocupacaoAtual != null) {
            if (this.ocupacaoAtual >= this.capacidadeMaxima) {
                this.situacao = StatusQuarto.OCUPADO;
            } else {
                this.situacao = StatusQuarto.DISPONIVEL;
            }
        }
    }

    /**
     * Informa se o quarto está atualmente disponível para internação.
     *
     * @return true se situacao for DISPONIVEL e possuir vaga livre.
     */
    public boolean isDisponivel() {
        return StatusQuarto.DISPONIVEL.equals(this.situacao) && temVagaDisponivel();
    }

    /**
     * Informa se o quarto atingiu a lotação máxima.
     *
     * @return true se situacao for OCUPADO ou ocupacao >= capacidadeMaxima.
     */
    public boolean isOcupado() {
        return StatusQuarto.OCUPADO.equals(this.situacao) || !temVagaDisponivel();
    }

    // --- Getters e Setters ---

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
        atualizarSituacao();
    }

    public Integer getOcupacaoAtual() {
        return ocupacaoAtual;
    }

    public void setOcupacaoAtual(Integer ocupacaoAtual) {
        this.ocupacaoAtual = ocupacaoAtual;
        atualizarSituacao();
    }

    public StatusQuarto getSituacao() {
        return situacao;
    }

    public void setSituacao(StatusQuarto situacao) {
        this.situacao = situacao;
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

    @Override
    public String toString() {
        return "Quarto{" +
                "id=" + id +
                ", numeroIdentificacao='" + numeroIdentificacao + '\'' +
                ", andar=" + andar +
                ", capacidadeMaxima=" + capacidadeMaxima +
                ", ocupacaoAtual=" + ocupacaoAtual +
                ", situacao=" + situacao +
                '}';
    }
}
