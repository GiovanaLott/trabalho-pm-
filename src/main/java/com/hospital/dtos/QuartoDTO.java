package com.hospital.dtos;

import com.hospital.entidades.Quarto;
import com.hospital.entidades.StatusQuarto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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

    public QuartoDTO() {
    }

    public QuartoDTO(Quarto quarto) {
        if (quarto != null) {
            this.id = quarto.getId();
            this.numeroIdentificacao = quarto.getNumeroIdentificacao();
            this.andar = quarto.getAndar();
            this.capacidadeMaxima = quarto.getCapacidadeMaxima();
            this.ocupacaoAtual = quarto.getOcupacaoAtual();
            this.situacao = quarto.getSituacao();
        }
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
}
