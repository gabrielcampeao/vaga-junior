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
        return new AbastecimentoResponse(
                entidade.getId(),
                BombaResponse.fromEntity(entidade.getBomba()),
                entidade.getDataHora(),
                entidade.getLitros(),
                entidade.getPrecoLitro(),
                entidade.getValorTotal()
        );
    }
}
