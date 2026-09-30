package com.gabrielcampeao.posto.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AbastecimentoRequest(
        @NotNull(message = "O ID da bomba é obrigatório")
        Long bombaId,

        @NotNull(message = "A quantidade de litros é obrigatória")
        @Positive(message = "A quantidade de litros deve ser positiva")
        @Digits(integer = 7, fraction = 3, message = "A quantidade de litros deve ter até 7 dígitos inteiros e 3 casas decimais")
        BigDecimal litros,

        @PastOrPresent(message = "A data e hora não pode estar no futuro")
        LocalDateTime dataHora
) {
}
