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
                                     BombaRepository bombaRepository) {
        return args -> {

            // Carga inicial de tipos de combustível (independente das bombas)
            if (tipoRepository.count() == 0) {
                tipoRepository.save(new TipoCombustivel("Gasolina Comum", new BigDecimal("5.899")));
                tipoRepository.save(new TipoCombustivel("Etanol", new BigDecimal("4.099")));
                tipoRepository.save(new TipoCombustivel("Diesel S10", new BigDecimal("6.199")));
            }

            // Carga inicial de bombas (independente dos tipos; busca por nome normalizado)
            if (bombaRepository.count() == 0) {
                tipoRepository.findAll().forEach(tipo -> {
                    String bombaId = switch (tipo.getNomeNormalizado()) {
                        case "gasolina comum" -> "B-01";
                        case "etanol" -> "B-02";
                        case "diesel s10" -> "B-03";
                        default -> null;
                    };
                    if (bombaId != null) {
                        bombaRepository.save(new Bomba(bombaId, tipo));
                    }
                });
            }
        };
    }
}
