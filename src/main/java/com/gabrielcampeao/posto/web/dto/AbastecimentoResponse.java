package com.gabrielcampeao.posto.web.dto;

import com.gabrielcampeao.posto.domain.Abastecimento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AbastecimentoResponse(
        Long id,
        BombaResponse bomba,
        LocalDateTime dataHora,
        BigDecimal litros,
        BigDecimal precoLitro,
        BigDecimal valorTotal
) {

    public static AbastecimentoResponse fromEntity(Abastecimento entidade) {
        BigDecimal litros = entidade.getLitros() != null ? entidade.getLitros().setScale(3, java.math.RoundingMode.HALF_UP) : null;
        BigDecimal precoLitro = entidade.getPrecoLitro() != null ? entidade.getPrecoLitro().setScale(3, java.math.RoundingMode.HALF_UP) : null;
        BigDecimal valorTotal = entidade.getValorTotal() != null ? entidade.getValorTotal().setScale(2, java.math.RoundingMode.HALF_UP) : null;
        return new AbastecimentoResponse(
                entidade.getId(),
                BombaResponse.fromEntity(entidade.getBomba()),
                entidade.getDataHora(),
                litros,
                precoLitro,
                valorTotal
        );
    }
}
