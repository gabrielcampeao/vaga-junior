package com.gabrielcampeao.posto.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        Map<String, String> campos
) {

    public static ErroResponse of(int status, String erro, String mensagem) {
        return new ErroResponse(LocalDateTime.now(), status, erro, mensagem, null);
    }

    public static ErroResponse of(int status, String erro, String mensagem, Map<String, String> campos) {
        return new ErroResponse(LocalDateTime.now(), status, erro, mensagem, campos);
    }
}
