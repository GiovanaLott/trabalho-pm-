package com.hospital.excecoes;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Corpo padrão (JSON) devolvido pela API em qualquer erro.
 *
 * @param timestamp momento do erro
 * @param status    código HTTP (ex.: 404)
 * @param error     categoria curta do erro
 * @param message   explicação para o cliente da API
 * @param path      rota que originou o erro
 * @param errors    detalhes por campo (somente em erros de validação)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RespostaErro(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> errors) {
}
