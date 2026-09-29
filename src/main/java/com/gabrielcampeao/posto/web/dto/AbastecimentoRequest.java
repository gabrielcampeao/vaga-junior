package com.gabrielcampeao.posto.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AbastecimentoRequest(
        @NotNull(message = "O ID da bomba é obrigatório")
        Long bombaId,

        @NotNull(message = "A quantidade de litros é obrigatória")
        @Positive(message = "A quantidade de litros deve ser positiva")
        BigDecimal litros,

        LocalDateTime dataHora
) {
}
