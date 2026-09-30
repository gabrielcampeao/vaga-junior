package com.gabrielcampeao.posto.service;

import com.gabrielcampeao.posto.domain.Abastecimento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculoAbastecimentoTest {

    @ParameterizedTest
    @CsvSource({
            "10.000, 5.899, 58.99",   // 58.990 -> 58.99 (exato)
            "10.500, 5.899, 61.94",   // 61.9395 -> 61.94 (arredonda pra cima por ser >= 5)
            "32.551, 4.099, 133.43",  // 133.426549 -> 133.43 (arredonda pra cima)
            "1.000, 5.894, 5.89",     // 5.894 -> 5.89 (arredonda pra baixo por ser < 5)
            "1.000, 5.895, 5.90",     // 5.895 -> 5.90 (HALF_UP no ponto medio)
            "0.500, 4.099, 2.05"      // 2.0495 -> 2.05
    })
    @DisplayName("Deve calcular valor total com arredondamento HALF_UP de duas casas decimais nas bordas")
    void deveCalcularValorTotalComArredondamentoCorreto(BigDecimal litros, BigDecimal precoLitro, BigDecimal valorEsperado) {
        BigDecimal valorCalculado = Abastecimento.calcularValorTotal(litros, precoLitro);
        assertEquals(valorEsperado, valorCalculado);
    }
}
