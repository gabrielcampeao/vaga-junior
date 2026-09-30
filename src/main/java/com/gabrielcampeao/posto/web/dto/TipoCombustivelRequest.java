package com.gabrielcampeao.posto.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TipoCombustivelRequest(
        @NotBlank(message = "O nome do combustível é obrigatório")
        @Size(max = 255, message = "O nome do combustível deve ter no máximo 255 caracteres")
        String nome,

        @NotNull(message = "O preço por litro é obrigatório")
        @Positive(message = "O preço por litro precisa ser positivo")
        @Digits(integer = 3, fraction = 3, message = "O preço por litro deve ter até 3 dígitos inteiros e 3 casas decimais")
        BigDecimal precoLitro
) {
}
