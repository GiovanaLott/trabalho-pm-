package com.hospital.dtos;

import com.hospital.entidades.TipoIsolamento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Dados que podem ser alterados em um quarto já cadastrado. Campos nulos são mantidos.
 * O tipo do quarto não pode ser alterado (seria outro quarto).
 */
public class QuartoUpdateDTO {

    @Size(min = 1, max = 20, message = "O número de identificação deve ter entre 1 e 20 caracteres")
    private String numeroIdentificacao;

    @Min(value = 0, message = "O andar deve ser 0 (térreo) ou maior")
    private Integer andar;

    @Min(value = 1, message = "A capacidade máxima deve ser de no mínimo 1")
    private Integer capacidadeMaxima;

    private Boolean permiteAcompanhante;

    private TipoIsolamento tipoIsolamento;

    public QuartoUpdateDTO() {
    }

    public QuartoUpdateDTO(String numeroIdentificacao, Integer andar, Integer capacidadeMaxima) {
        this.numeroIdentificacao = numeroIdentificacao;
        this.andar = andar;
        this.capacidadeMaxima = capacidadeMaxima;
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

    public Boolean getPermiteAcompanhante() {
        return permiteAcompanhante;
    }

    public void setPermiteAcompanhante(Boolean permiteAcompanhante) {
        this.permiteAcompanhante = permiteAcompanhante;
    }

    public TipoIsolamento getTipoIsolamento() {
        return tipoIsolamento;
    }

    public void setTipoIsolamento(TipoIsolamento tipoIsolamento) {
        this.tipoIsolamento = tipoIsolamento;
    }
}
