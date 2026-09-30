package com.gabrielcampeao.posto.config;

import com.gabrielcampeao.posto.domain.Bomba;
import com.gabrielcampeao.posto.domain.TipoCombustivel;
import com.gabrielcampeao.posto.repository.BombaRepository;
import com.gabrielcampeao.posto.repository.TipoCombustivelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

@Configuration
@Profile("!test")
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(TipoCombustivelRepository tipoRepository,
                                     BombaRepository bombaRepository,
                                     MigracaoNomesNormalizados migracao) {
        return args -> {
            // Migração de dados legados: preenche os campos normalizados de forma segura,
            // tratando duplicatas preexistentes com sufixos ao invés de lançar exceção.
            migracao.migrar();

            if (tipoRepository.count() == 0) {
                TipoCombustivel gasolina = tipoRepository.save(new TipoCombustivel("Gasolina Comum", new BigDecimal("5.899")));
                TipoCombustivel etanol = tipoRepository.save(new TipoCombustivel("Etanol", new BigDecimal("4.099")));
                TipoCombustivel diesel = tipoRepository.save(new TipoCombustivel("Diesel S10", new BigDecimal("6.199")));

                if (bombaRepository.count() == 0) {
                    bombaRepository.save(new Bomba("B-01", gasolina));
                    bombaRepository.save(new Bomba("B-02", etanol));
                    bombaRepository.save(new Bomba("B-03", diesel));
                }
            }
        };
    }
}
