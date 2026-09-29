package com.gabrielcampeao.posto.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TipoCombustivelRequest(
        @NotBlank(message = "O nome do combustível é obrigatório")
        String nome,

        @NotNull(message = "O preço por litro é obrigatório")
        @Positive(message = "O preço por litro precisa ser positivo")
        BigDecimal precoLitro
) {
}
