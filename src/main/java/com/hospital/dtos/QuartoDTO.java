package com.hospital.dtos;

import com.hospital.entidades.Quarto;
import com.hospital.entidades.StatusQuarto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para transferência de dados de Quarto hospitalar (entrada e saída da API REST).
 *
 * @author Guilherme Oliveira
 */
public class QuartoDTO {

    private Long id;

    @NotBlank(message = "O número de identificação do quarto é obrigatório")
    private String numeroIdentificacao;

    @NotNull(message = "O andar é obrigatório")
    private Integer andar;

    @NotNull(message = "A capacidade máxima é obrigatória")
    @Min(value = 1, message = "A capacidade máxima deve ser de no mínimo 1")
    private Integer capacidadeMaxima;

    private Integer ocupacaoAtual;
    private StatusQuarto situacao;
    private Integer vagasDisponiveis;

    public QuartoDTO() {
    }

    public QuartoDTO(Long id, String numeroIdentificacao, Integer andar, Integer capacidadeMaxima, Integer ocupacaoAtual, StatusQuarto situacao) {
        this.id = id;
        this.numeroIdentificacao = numeroIdentificacao;
        this.andar = andar;
        this.capacidadeMaxima = capacidadeMaxima;
        this.ocupacaoAtual = ocupacaoAtual != null ? ocupacaoAtual : 0;
        this.situacao = situacao != null ? situacao : StatusQuarto.DISPONIVEL;
        this.vagasDisponiveis = Math.max(0, this.capacidadeMaxima - this.ocupacaoAtual);
    }

    public QuartoDTO(Quarto quarto) {
        if (quarto != null) {
            this.id = quarto.getId();
            this.numeroIdentificacao = quarto.getNumeroIdentificacao();
            this.andar = quarto.getAndar();
            this.capacidadeMaxima = quarto.getCapacidadeMaxima();
            this.ocupacaoAtual = quarto.getOcupacaoAtual();
            this.situacao = quarto.getSituacao();
            this.vagasDisponiveis = quarto.getVagasDisponiveis();
        }
    }

    /**
     * Converte este DTO em uma nova entidade Quarto.
     *
     * @return Entidade Quarto.
     */
    public Quarto paraEntidade() {
        return new Quarto(this.id, this.numeroIdentificacao, this.andar, this.capacidadeMaxima, this.ocupacaoAtual, this.situacao);
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

    public Integer getVagasDisponiveis() {
        return vagasDisponiveis;
    }

    public void setVagasDisponiveis(Integer vagasDisponiveis) {
        this.vagasDisponiveis = vagasDisponiveis;
    }

    @Override
    public String toString() {
        return "QuartoDTO{" +
                "id=" + id +
                ", numeroIdentificacao='" + numeroIdentificacao + '\'' +
                ", andar=" + andar +
                ", capacidadeMaxima=" + capacidadeMaxima +
                ", ocupacaoAtual=" + ocupacaoAtual +
                ", situacao=" + situacao +
                ", vagasDisponiveis=" + vagasDisponiveis +
                '}';
    }
}
