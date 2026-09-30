package com.gabrielcampeao.posto.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Dados para registrar ou atualizar um abastecimento. Informe litros OU valor, nunca ambos.")
public record AbastecimentoRequest(
        @NotNull(message = "O ID da bomba é obrigatório")
        Long bombaId,

        @Positive(message = "A quantidade de litros deve ser positiva")
        @Digits(integer = 7, fraction = 3, message = "A quantidade de litros deve ter até 7 dígitos inteiros e 3 casas decimais")
        @Schema(description = "Quantidade de litros abastecidos (opcional se valor for informado)")
        BigDecimal litros,

        @PastOrPresent(message = "A data e hora não pode estar no futuro")
        LocalDateTime dataHora,

        @Positive(message = "O valor deve ser positivo")
        @Digits(integer = 8, fraction = 2, message = "O valor deve ter até 8 dígitos inteiros e 2 casas decimais")
        @Schema(description = "Valor total em reais (opcional se litros for informado)")
        BigDecimal valor
) {
}
