package com.gabrielcampeao.posto.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BombaRequest(
        @NotBlank(message = "O identificador da bomba é obrigatório")
        @Size(max = 255, message = "O identificador da bomba deve ter no máximo 255 caracteres")
        String identificador,

        @NotNull(message = "O ID do tipo de combustível é obrigatório")
        Long tipoCombustivelId
) {
}
