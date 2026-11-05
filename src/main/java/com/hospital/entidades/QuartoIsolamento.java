package com.hospital.entidades;

import com.hospital.excecoes.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Quarto individual (exatamente 1 leito) para pacientes que exigem isolamento.
 * O tipo de precaução (contato, gotículas ou aerossóis) é obrigatório.
 */
@Entity
@DiscriminatorValue("ISOLAMENTO")
public class QuartoIsolamento extends Quarto {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_isolamento", length = 20)
    private TipoIsolamento tipoIsolamento;

    public QuartoIsolamento() {
        super();
    }

    public QuartoIsolamento(Long id, String numeroIdentificacao, Integer andar, Integer ocupacaoAtual,
                            StatusQuarto situacao, TipoIsolamento tipoIsolamento) {
        super(id, numeroIdentificacao, andar, 1, ocupacaoAtual, situacao);
        this.tipoIsolamento = tipoIsolamento;
    }

    @Override
    public TipoQuarto getTipo() {
        return TipoQuarto.ISOLAMENTO;
    }

    @Override
    public void validarConfiguracao() {
        super.validarConfiguracao();
        if (tipoIsolamento == null) {
            throw new RegraNegocioException(
                    "Informe o tipo de isolamento do quarto (CONTATO, GOTICULAS ou AEROSSOIS).");
        }
    }

    public TipoIsolamento getTipoIsolamento() {
        return tipoIsolamento;
    }

    public void setTipoIsolamento(TipoIsolamento tipoIsolamento) {
        this.tipoIsolamento = tipoIsolamento;
    }
}
