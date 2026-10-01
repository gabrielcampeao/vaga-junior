package com.gabrielcampeao.posto.config;

import com.gabrielcampeao.posto.domain.Bomba;
import com.gabrielcampeao.posto.domain.TipoCombustivel;
import com.gabrielcampeao.posto.repository.BombaRepository;
import com.gabrielcampeao.posto.repository.TipoCombustivelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@Import(MigracaoNomesNormalizados.class)
class MigracaoNomesNormalizadosTest {

    @Autowired
    private TipoCombustivelRepository tipoCombustivelRepository;

    @Autowired
    private BombaRepository bombaRepository;

    @Autowired
    private MigracaoNomesNormalizados migracao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        bombaRepository.deleteAll();
        tipoCombustivelRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve preencher nomeNormalizado de registros legados com campo nulo")
    void devePreencherNomeNormalizadoDeRegistrosLegados() {
        // Insere registro legado com nome_normalizado = NULL via SQL nativo
        jdbcTemplate.update(
                "INSERT INTO tipo_combustivel (nome, nome_normalizado, preco_litro) VALUES (?, NULL, ?)",
                "Gasolina Comum", 5.899);

        migracao.migrar();

        List<TipoCombustivel> todos = tipoCombustivelRepository.findAll();
        assertEquals(1, todos.size());
        assertNotNull(todos.get(0).getNomeNormalizado());
        assertEquals("gasolina comum", todos.get(0).getNomeNormalizado());
    }

    @Test
    @DisplayName("Deve adicionar sufixo (dup 2) quando dois registros legados normalizam para o mesmo valor")
    void deveAdicionarSufixoQuandoNormalizadosDuplicados() {
        // Insere dois registros legados que normalizam para "etanol"
        jdbcTemplate.update(
                "INSERT INTO tipo_combustivel (nome, nome_normalizado, preco_litro) VALUES (?, NULL, ?)",
                "Etanol", 4.099);
        jdbcTemplate.update(
                "INSERT INTO tipo_combustivel (nome, nome_normalizado, preco_litro) VALUES (?, NULL, ?)",
                "  etanol ", 4.199);

        migracao.migrar();

        List<TipoCombustivel> todos = tipoCombustivelRepository.findAll();
        assertEquals(2, todos.size());

        // Ambos devem ter nomeNormalizado não nulo
        todos.forEach(t -> assertNotNull(t.getNomeNormalizado(),
                "nomeNormalizado deve ser preenchido para: " + t.getNome()));

        // Os valores normalizados devem ser únicos
        long distintos = todos.stream().map(TipoCombustivel::getNomeNormalizado).distinct().count();
        assertEquals(2, distintos, "nomeNormalizado deve ser único entre os registros");

        // Um deles deve conter o sufixo (dup 2)
        boolean algumComSufixo = todos.stream()
                .anyMatch(t -> t.getNomeNormalizado().contains("(dup 2)"));
        assertTrue(algumComSufixo, "Um dos registros deve ter sufixo (dup 2)");
    }

    @Test
    @DisplayName("Deve ser idempotente: executar migrar() duas vezes não altera registros já migrados")
    void deveSerIdempotente() {
        jdbcTemplate.update(
                "INSERT INTO tipo_combustivel (nome, nome_normalizado, preco_litro) VALUES (?, NULL, ?)",
                "Diesel", 6.199);

        migracao.migrar();

        List<TipoCombustivel> aposFirstMigracao = tipoCombustivelRepository.findAll();
        String nomeApos1 = aposFirstMigracao.get(0).getNome();
        String normApos1 = aposFirstMigracao.get(0).getNomeNormalizado();

        migracao.migrar();

        List<TipoCombustivel> aposSegundaMigracao = tipoCombustivelRepository.findAll();
        assertEquals(nomeApos1, aposSegundaMigracao.get(0).getNome());
        assertEquals(normApos1, aposSegundaMigracao.get(0).getNomeNormalizado());
    }

    @Test
    @DisplayName("Deve preencher identificadorNormalizado de bombas legadas com campo nulo")
    void devePreencherIdentificadorNormalizadoDeBombasLegadas() {
        // Precisa de um tipo para a FK
        jdbcTemplate.update(
                "INSERT INTO tipo_combustivel (id, nome, nome_normalizado, preco_litro) VALUES (?, ?, ?, ?)",
                999L, "Gas Bomba", "gas bomba", 5.000);

        jdbcTemplate.update(
                "INSERT INTO bomba (identificador, identificador_normalizado, tipo_combustivel_id) VALUES (?, NULL, ?)",
                "B-01", 999L);

        migracao.migrar();

        List<Bomba> todas = bombaRepository.findAll();
        assertEquals(1, todas.size());
        assertNotNull(todas.get(0).getIdentificadorNormalizado());
        assertEquals("b-01", todas.get(0).getIdentificadorNormalizado());
    }

    @Test
    @DisplayName("Deve adicionar sufixo (dup 2) para bombas legadas com identificador duplicado")
    void deveAdicionarSufixoParaBombasComIdentificadorDuplicado() {
        jdbcTemplate.update(
                "INSERT INTO tipo_combustivel (id, nome, nome_normalizado, preco_litro) VALUES (?, ?, ?, ?)",
                998L, "Gas Bomba2", "gas bomba2", 5.000);

        jdbcTemplate.update(
                "INSERT INTO bomba (identificador, identificador_normalizado, tipo_combustivel_id) VALUES (?, NULL, ?)",
                "B-DUP", 998L);
        jdbcTemplate.update(
                "INSERT INTO bomba (identificador, identificador_normalizado, tipo_combustivel_id) VALUES (?, NULL, ?)",
                "  b-dup ", 998L);

        migracao.migrar();

        List<Bomba> todas = bombaRepository.findAll();
        assertEquals(2, todas.size());

        todas.forEach(b -> assertNotNull(b.getIdentificadorNormalizado()));

        long distintos = todas.stream().map(Bomba::getIdentificadorNormalizado).distinct().count();
        assertEquals(2, distintos);

        boolean algumComSufixo = todas.stream()
                .anyMatch(b -> b.getIdentificadorNormalizado().contains("(dup 2)"));
        assertTrue(algumComSufixo, "Uma das bombas deve ter sufixo (dup 2)");
    }
}
