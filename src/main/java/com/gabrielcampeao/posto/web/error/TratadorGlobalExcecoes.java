package com.gabrielcampeao.posto.web.error;

import com.gabrielcampeao.posto.service.exception.LimiteLitrosExcedidoException;
import com.gabrielcampeao.posto.service.exception.NomeDuplicadoException;
import com.gabrielcampeao.posto.service.exception.RecursoEmUsoException;
import com.gabrielcampeao.posto.service.exception.RecursoNaoEncontradoException;
import com.gabrielcampeao.posto.service.exception.RequisicaoInvalidaException;
import com.gabrielcampeao.posto.web.dto.ErroResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice
public class TratadorGlobalExcecoes {

    private static final Logger log = LoggerFactory.getLogger(TratadorGlobalExcecoes.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        ErroResponse erro = ErroResponse.of(HttpStatus.NOT_FOUND.value(), "Não encontrado", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(RecursoEmUsoException.class)
    public ResponseEntity<ErroResponse> handleEmUso(RecursoEmUsoException ex) {
        ErroResponse erro = ErroResponse.of(HttpStatus.CONFLICT.value(), "Conflito", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(NomeDuplicadoException.class)
    public ResponseEntity<ErroResponse> handleNomeDuplicado(NomeDuplicadoException ex) {
        ErroResponse erro = ErroResponse.of(HttpStatus.CONFLICT.value(), "Conflito", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(LimiteLitrosExcedidoException.class)
    public ResponseEntity<ErroResponse> handleLimiteLitrosExcedido(LimiteLitrosExcedidoException ex) {
        ErroResponse erro = ErroResponse.of(HttpStatus.BAD_REQUEST.value(), "Limite Excedido", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ErroResponse> handleRequisicaoInvalida(RequisicaoInvalidaException ex) {
        ErroResponse erro = ErroResponse.of(HttpStatus.BAD_REQUEST.value(), "Requisição Inválida", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> handleJsonMalformado(HttpMessageNotReadableException ex) {
        ErroResponse erro = ErroResponse.of(HttpStatus.BAD_REQUEST.value(), "Requisição Inválida", "Corpo da requisição ausente ou com formato incorreto");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> handleTipoParametroInvalido(MethodArgumentTypeMismatchException ex) {
        String msg = String.format("O parâmetro '%s' recebeu o valor '%s', que é de um tipo inválido", ex.getName(), ex.getValue());
        ErroResponse erro = ErroResponse.of(HttpStatus.BAD_REQUEST.value(), "Requisição Inválida", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErroResponse> handleOrdenacaoInvalida(PropertyReferenceException ex) {
        String msg = String.format("A propriedade de ordenação '%s' não existe", ex.getPropertyName());
        ErroResponse erro = ErroResponse.of(HttpStatus.BAD_REQUEST.value(), "Requisição Inválida", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> handleViolacaoIntegridade(DataIntegrityViolationException ex) {
        ErroResponse erro = ErroResponse.of(HttpStatus.CONFLICT.value(), "Conflito de Integridade", "Operação não permitida por violação de integridade nos dados");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                campos.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        ErroResponse erro = ErroResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Validação",
                "Um ou mais campos estão inválidos",
                campos
        );

        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> handleNoResourceFound(NoResourceFoundException ex) {
        String path = ex.getResourcePath();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        ErroResponse erro = ErroResponse.of(HttpStatus.NOT_FOUND.value(), "Não encontrado", "Recurso não encontrado: " + path);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String msg = String.format("O método %s não é suportado para este recurso", ex.getMethod());
        ErroResponse erro = ErroResponse.of(HttpStatus.METHOD_NOT_ALLOWED.value(), "Método não permitido", msg);
        HttpHeaders headers = new HttpHeaders();
        Set<HttpMethod> supported = ex.getSupportedHttpMethods();
        if (supported != null && !supported.isEmpty()) {
            headers.setAllow(supported);
        }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).body(erro);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        ErroResponse erro = ErroResponse.of(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "Tipo de mídia não suportado", "Content-Type não suportado; use application/json");
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleExcecaoGenerica(Exception ex) {
        log.error("Erro interno não tratado na aplicação: ", ex);
        ErroResponse erro = ErroResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erro Interno", "Ocorreu um erro interno inesperado no servidor");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}
