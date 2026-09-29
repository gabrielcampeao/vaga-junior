package com.gabrielcampeao.posto.web.error;

import com.gabrielcampeao.posto.service.exception.NomeDuplicadoException;
import com.gabrielcampeao.posto.service.exception.RecursoEmUsoException;
import com.gabrielcampeao.posto.service.exception.RecursoNaoEncontradoException;
import com.gabrielcampeao.posto.web.dto.ErroResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class TratadorGlobalExcecoes {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        ErroResponse erro = ErroResponse.of(404, "Não encontrado", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(RecursoEmUsoException.class)
    public ResponseEntity<ErroResponse> handleEmUso(RecursoEmUsoException ex) {
        ErroResponse erro = ErroResponse.of(409, "Conflito", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(NomeDuplicadoException.class)
    public ResponseEntity<ErroResponse> handleNomeDuplicado(NomeDuplicadoException ex) {
        ErroResponse erro = ErroResponse.of(409, "Conflito", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                campos.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        Map<String, Object> corpo = new HashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("status", 400);
        corpo.put("erro", "Validação");
        corpo.put("mensagem", "Um ou mais campos estão inválidos");
        corpo.put("campos", campos);

        return ResponseEntity.badRequest().body(corpo);
    }
}
