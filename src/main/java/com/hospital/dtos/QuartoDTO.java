package com.hospital.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hospital.entidades.Apartamento;
import com.hospital.entidades.Quarto;
import com.hospital.entidades.QuartoIsolamento;
import com.hospital.entidades.StatusQuarto;
import com.hospital.entidades.TipoIsolamento;
import com.hospital.entidades.TipoQuarto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entrada e saída de dados de quartos.
 *
 * <p>No cadastro, {@code tipo} define a subclasse criada (se omitido, assume ENFERMARIA).
 * {@code permiteAcompanhante} só se aplica a APARTAMENTO e {@code tipoIsolamento} só a ISOLAMENTO.
 * Ocupação, situação e leitos disponíveis são calculados pelo sistema.
 */
public class QuartoDTO {

    private Long id;

    @NotBlank(message = "O número de identificação do quarto é obrigatório")
    @Size(max = 20, message = "O número de identificação deve ter no máximo 20 caracteres")
    private String numeroIdentificacao;

    @NotNull(message = "O andar é obrigatório")
    @Min(value = 0, message = "O andar deve ser 0 (térreo) ou maior")
    private Integer andar;

    @NotNull(message = "A capacidade máxima é obrigatória")
    @Min(value = 1, message = "A capacidade máxima deve ser de no mínimo 1")
    private Integer capacidadeMaxima;

    private TipoQuarto tipo;

    /** Só para APARTAMENTO. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean permiteAcompanhante;

    /** Só para ISOLAMENTO. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TipoIsolamento tipoIsolamento;

    private Integer ocupacaoAtual;

    private StatusQuarto situacao;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer leitosDisponiveis;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String descricaoTipo;

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
            this.tipo = quarto.getTipo();
            this.descricaoTipo = quarto.getTipo().getDescricao();
            this.leitosDisponiveis = quarto.getLeitosDisponiveis();
            if (quarto instanceof Apartamento apartamento) {
                this.permiteAcompanhante = apartamento.getPermiteAcompanhante();
            } else if (quarto instanceof QuartoIsolamento isolamento) {
                this.tipoIsolamento = isolamento.getTipoIsolamento();
            }
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

    public TipoQuarto getTipo() {
        return tipo;
    }

    public void setTipo(TipoQuarto tipo) {
        this.tipo = tipo;
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

    public Integer getLeitosDisponiveis() {
        return leitosDisponiveis;
    }

    public String getDescricaoTipo() {
        return descricaoTipo;
    }
}
