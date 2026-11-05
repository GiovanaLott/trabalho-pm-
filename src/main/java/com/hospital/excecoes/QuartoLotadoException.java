package com.hospital.excecoes;

/** O quarto atingiu sua capacidade máxima de ocupação. */
public class QuartoLotadoException extends QuartoIndisponivelException {
    public QuartoLotadoException(String message) {
        super(message);
    }
}
