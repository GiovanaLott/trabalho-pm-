package com.hospital.entidades;

import com.hospital.excecoes.RegraNegocioException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Leitos de UTI (1 a 10). Toda internação em UTI exige uma justificativa clínica registrada
 * nas observações da internação.
 */
@Entity
@DiscriminatorValue("UTI")
public class UnidadeTerapiaIntensiva extends Quarto {

    public UnidadeTerapiaIntensiva() {
        super();
    }

    public UnidadeTerapiaIntensiva(Long id, String numeroIdentificacao, Integer andar, Integer capacidadeMaxima,
                                   Integer ocupacaoAtual, StatusQuarto situacao) {
        super(id, numeroIdentificacao, andar, capacidadeMaxima, ocupacaoAtual, situacao);
    }

    @Override
    public TipoQuarto getTipo() {
        return TipoQuarto.UTI;
    }

    @Override
    protected void validarRequisitosDoTipo(Internacao internacao) {
        if (internacao == null || internacao.getObservacoes() == null || internacao.getObservacoes().isBlank()) {
            throw new RegraNegocioException(
                    "A internação em UTI exige uma justificativa clínica no campo de observações.");
        }
    }
}
