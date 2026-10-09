package com.hospital.excecoes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento global de erros da API. Toda exceção vira uma resposta JSON padronizada
 * ({@link RespostaErro}) com o código HTTP adequado, sem expor detalhes internos.
 *
 * <p>Estende {@link ResponseEntityExceptionHandler} para cobrir também as exceções do próprio
 * Spring MVC (JSON malformado, parâmetro com tipo errado, rota inexistente, método não
 * permitido, tipo de mídia não suportado etc.), que antes caíam no erro 500 genérico.
 *
 * <pre>
 *  404  RecursoNaoEncontradoException, rota inexistente
 *  409  ChoqueHorarioException, QuartoLotadoException, violação de integridade, conflito de concorrência
 *  400  RegraNegocioException, validação de campos, JSON inválido, parâmetro inválido
 *  405  método HTTP não permitido
 *  415  tipo de mídia não suportado
 *  500  qualquer erro inesperado (detalhes apenas no log do servidor)
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ------------------------------------------------------------------
    // Exceções de domínio (regras de negócio)
    // ------------------------------------------------------------------

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<RespostaErro> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex,
                                                                   HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(),
                request.getRequestURI(), null);
    }

    /** Choque de horário e quarto lotado: o pedido conflita com o estado atual dos dados (409). */
    @ExceptionHandler({ChoqueHorarioException.class, QuartoLotadoException.class})
    public ResponseEntity<RespostaErro> handleConflitoDeNegocio(RegraNegocioException ex,
                                                                HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, "Conflito de regra de negócio", ex.getMessage(),
                request.getRequestURI(), null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<RespostaErro> handleRegraNegocio(RegraNegocioException ex,
                                                           HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "Violação de regra de negócio", ex.getMessage(),
                request.getRequestURI(), null);
    }

    // ------------------------------------------------------------------
    // Persistência e concorrência
    // ------------------------------------------------------------------

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespostaErro> handleIntegridade(DataIntegrityViolationException ex,
                                                          HttpServletRequest request) {
        log.warn("Violação de integridade em {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return resposta(HttpStatus.CONFLICT, "Violação de integridade dos dados",
                "A operação viola uma restrição do banco de dados (por exemplo, valor único já cadastrado ou registro em uso).",
                request.getRequestURI(), null);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<RespostaErro> handleConcorrencia(OptimisticLockingFailureException ex,
                                                           HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, "Conflito de concorrência",
                "O registro foi alterado por outra operação ao mesmo tempo. Tente novamente.",
                request.getRequestURI(), null);
    }

    // ------------------------------------------------------------------
    // Validação
    // ------------------------------------------------------------------

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RespostaErro> handleConstraintViolation(ConstraintViolationException ex,
                                                                  HttpServletRequest request) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> erros.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Erro de validação dos campos",
                "Um ou mais parâmetros são inválidos.", request.getRequestURI(), erros);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> erros.putIfAbsent(e.getField(), e.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(e -> erros.putIfAbsent(e.getObjectName(), e.getDefaultMessage()));
        return ResponseEntity.status(status).headers(headers).body(
                criar(HttpStatus.BAD_REQUEST, "Erro de validação dos campos",
                        "Um ou mais campos são inválidos.", caminho(request), erros));
    }

    // ------------------------------------------------------------------
    // Erros de requisição tratados pelo Spring MVC
    // ------------------------------------------------------------------

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return ResponseEntity.status(status).headers(headers).body(
                criar(HttpStatus.BAD_REQUEST, "Requisição inválida",
                        "O corpo da requisição está ausente, malformado ou contém valor em formato inválido "
                                + "(ex.: datas devem seguir o padrão yyyy-MM-dd ou yyyy-MM-ddTHH:mm:ss).",
                        caminho(request), null));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
                                                        HttpHeaders headers,
                                                        HttpStatusCode status,
                                                        WebRequest request) {
        String parametro = ex.getPropertyName() != null ? ex.getPropertyName() : "informado";
        return ResponseEntity.status(status).headers(headers).body(
                criar(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                        "O valor '" + ex.getValue() + "' não é válido para o parâmetro " + parametro + ".",
                        caminho(request), null));
    }

    /**
     * Ponto único por onde passam as demais exceções do Spring MVC (rota inexistente, método HTTP
     * não permitido, tipo de mídia não suportado, parâmetro obrigatório ausente etc.).
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (status.is5xxServerError()) {
            log.error("Erro do servidor em {}", caminho(request), ex);
        }

        String erro;
        String mensagem;
        switch (status) {
            case NOT_FOUND -> {
                erro = "Recurso não encontrado";
                mensagem = "Nenhum recurso encontrado para esta rota.";
            }
            case METHOD_NOT_ALLOWED -> {
                erro = "Método não permitido";
                mensagem = "O método HTTP utilizado não é permitido para este recurso.";
            }
            case UNSUPPORTED_MEDIA_TYPE -> {
                erro = "Tipo de mídia não suportado";
                mensagem = "Envie o corpo da requisição como application/json.";
            }
            case BAD_REQUEST -> {
                erro = "Requisição inválida";
                mensagem = "A requisição possui parâmetros ausentes ou inválidos.";
            }
            default -> {
                erro = status.getReasonPhrase();
                mensagem = status.is5xxServerError()
                        ? "Ocorreu um erro inesperado. Tente novamente mais tarde."
                        : "Não foi possível processar a requisição.";
            }
        }
        return ResponseEntity.status(statusCode).headers(headers)
                .body(criar(status, erro, mensagem, caminho(request), null));
    }

    // ------------------------------------------------------------------
    // Qualquer outro erro (rede de segurança)
    // ------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErro> handleErroInesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {}", request.getRequestURI(), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", request.getRequestURI(), null);
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private ResponseEntity<RespostaErro> resposta(HttpStatus status, String erro, String mensagem,
                                                  String caminho, Map<String, String> erros) {
        return ResponseEntity.status(status).body(criar(status, erro, mensagem, caminho, erros));
    }

    private RespostaErro criar(HttpStatus status, String erro, String mensagem,
                               String caminho, Map<String, String> erros) {
        return new RespostaErro(LocalDateTime.now(), status.value(), erro, mensagem, caminho, erros);
    }

    private String caminho(WebRequest request) {
        if (request instanceof ServletWebRequest servletRequest) {
            return servletRequest.getRequest().getRequestURI();
        }
        return null;
    }
}
