package com.gabrielcampeao.posto.web.dto;

import java.math.BigDecimal;

public record ResumoVendasResponse(
        String tipoCombustivel,
        BigDecimal totalLitros,
        BigDecimal valorTotalFaturado
) {
}
