package com.hospital.entidades;

import jakarta.persistence.*;
import java.util.Objects;

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

    public Quarto() {
    }

    public Quarto(Long id, String numeroIdentificacao, Integer andar, Integer capacidadeMaxima, Integer ocupacaoAtual, StatusQuarto situacao) {
        this.id = id;
        this.numeroIdentificacao = numeroIdentificacao;
        this.andar = andar;
        this.capacidadeMaxima = capacidadeMaxima;
        this.ocupacaoAtual = ocupacaoAtual != null ? ocupacaoAtual : 0;
        this.situacao = situacao != null ? situacao : StatusQuarto.DISPONIVEL;
    }

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

    public void incrementarOcupacao() {
        this.ocupacaoAtual++;
        if (this.ocupacaoAtual >= this.capacidadeMaxima) {
            this.situacao = StatusQuarto.OCUPADO;
        }
    }

    public void decrementarOcupacao() {
        if (this.ocupacaoAtual > 0) {
            this.ocupacaoAtual--;
        }
        if (this.ocupacaoAtual < this.capacidadeMaxima) {
            this.situacao = StatusQuarto.DISPONIVEL;
        }
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
