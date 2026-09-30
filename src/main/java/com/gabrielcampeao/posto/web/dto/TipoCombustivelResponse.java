package com.gabrielcampeao.posto.web.dto;

import com.gabrielcampeao.posto.domain.TipoCombustivel;

import java.math.BigDecimal;

public record TipoCombustivelResponse(
        Long id,
        String nome,
        BigDecimal precoLitro
) {

    public static TipoCombustivelResponse fromEntity(TipoCombustivel entidade) {
        BigDecimal precoLitro = entidade.getPrecoLitro() != null ? entidade.getPrecoLitro().setScale(3, java.math.RoundingMode.HALF_UP) : null;
        return new TipoCombustivelResponse(
                entidade.getId(),
                entidade.getNome(),
                precoLitro
        );
    }
}
