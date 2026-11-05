package com.hospital.excecoes;

/** O quarto não pode receber pacientes no momento (em manutenção ou sem leitos livres). */
public class QuartoIndisponivelException extends RegraNegocioException {
    public QuartoIndisponivelException(String message) {
        super(message);
    }
}
