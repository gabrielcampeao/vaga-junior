package com.gabrielcampeao.posto.web.dto;

import com.gabrielcampeao.posto.domain.Bomba;

public record BombaResponse(
        Long id,
        String identificador,
        TipoCombustivelResponse tipoCombustivel
) {

    public static BombaResponse fromEntity(Bomba entidade) {
        return new BombaResponse(
                entidade.getId(),
                entidade.getIdentificador(),
                TipoCombustivelResponse.fromEntity(entidade.getTipoCombustivel())
        );
    }
}
