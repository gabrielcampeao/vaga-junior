package com.gabrielcampeao.posto.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BombaRequest(
        @NotBlank(message = "O identificador da bomba é obrigatório")
        String identificador,

        @NotNull(message = "O ID do tipo de combustível é obrigatório")
        Long tipoCombustivelId
) {
}
