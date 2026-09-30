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

    @ParameterizedTest
    @CsvSource({
            "50.00, 5.000, 10.000",   // 50 / 5 = 10.000 (exato)
            "100.00, 5.899, 16.952",  // 100 / 5.899 = 16.9520... -> 16.952 (HALF_UP)
            "1.00, 3.000, 0.333",     // 1 / 3 = 0.333... -> 0.333
            "10.00, 3.000, 3.333",    // 10 / 3 = 3.333... -> 3.333
            "0.01, 5.899, 0.002",     // 0.01 / 5.899 = 0.001695... -> 0.002 (HALF_UP)
            "99.99, 4.099, 24.394"    // 99.99 / 4.099 = 24.3937... -> 24.394 (HALF_UP)
    })
    @DisplayName("Deve calcular litros a partir do valor com arredondamento HALF_UP de tres casas decimais")
    void deveCalcularLitrosComArredondamentoCorreto(BigDecimal valor, BigDecimal precoLitro, BigDecimal litrosEsperados) {
        BigDecimal litrosCalculados = Abastecimento.calcularLitros(valor, precoLitro);
        assertEquals(litrosEsperados, litrosCalculados);
    }
}
