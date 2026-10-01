package com.gabrielcampeao.posto.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabrielcampeao.posto.domain.Bomba;
import com.gabrielcampeao.posto.domain.TipoCombustivel;
import com.gabrielcampeao.posto.repository.AbastecimentoRepository;
import com.gabrielcampeao.posto.repository.BombaRepository;
import com.gabrielcampeao.posto.repository.TipoCombustivelRepository;
import com.gabrielcampeao.posto.web.dto.AbastecimentoRequest;
import com.gabrielcampeao.posto.web.dto.BombaRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BombaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TipoCombustivelRepository tipoCombustivelRepository;

    @Autowired
    private BombaRepository bombaRepository;

    @Autowired
    private AbastecimentoRepository abastecimentoRepository;

    @BeforeEach
    void setup() {
        abastecimentoRepository.deleteAll();
        bombaRepository.deleteAll();
        tipoCombustivelRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve retornar 409 ao trocar combustível de bomba que já possui abastecimentos")
    void deveRetornar409AoTrocarCombustivelDeBombaComAbastecimentos() throws Exception {
        TipoCombustivel gasolina = tipoCombustivelRepository.save(new TipoCombustivel("Gasolina T1", new BigDecimal("5.500")));
        TipoCombustivel etanol = tipoCombustivelRepository.save(new TipoCombustivel("Etanol T1", new BigDecimal("3.800")));
        Bomba bomba = bombaRepository.save(new Bomba("B-30", gasolina));

        AbastecimentoRequest abRequest = new AbastecimentoRequest(bomba.getId(), new BigDecimal("10.000"), null, null);
        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(abRequest)))
                .andExpect(status().isCreated());

        BombaRequest bombaUpdate = new BombaRequest("B-30", etanol.getId());
        mockMvc.perform(put("/api/bombas/" + bomba.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bombaUpdate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("Deve permitir trocar combustível de bomba sem abastecimentos")
    void devePermitirTrocarCombustivelDeBombaSemAbastecimentos() throws Exception {
        TipoCombustivel gasolina = tipoCombustivelRepository.save(new TipoCombustivel("Gasolina T2", new BigDecimal("5.500")));
        TipoCombustivel etanol = tipoCombustivelRepository.save(new TipoCombustivel("Etanol T2", new BigDecimal("3.800")));
        Bomba bomba = bombaRepository.save(new Bomba("B-31", gasolina));

        BombaRequest bombaUpdate = new BombaRequest("B-31", etanol.getId());
        mockMvc.perform(put("/api/bombas/" + bomba.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bombaUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoCombustivel.nome", is("Etanol T2")));
    }

    @Test
    @DisplayName("Deve permitir renomear bomba com abastecimentos sem alterar combustível")
    void devePermitirRenomearBombaComAbastecimentos() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Diesel T3", new BigDecimal("6.200")));
        Bomba bomba = bombaRepository.save(new Bomba("B-32", tipo));

        AbastecimentoRequest abRequest = new AbastecimentoRequest(bomba.getId(), new BigDecimal("15.000"), null, null);
        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(abRequest)))
                .andExpect(status().isCreated());

        BombaRequest bombaUpdate = new BombaRequest("B-32-NOVO", tipo.getId());
        mockMvc.perform(put("/api/bombas/" + bomba.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bombaUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificador", is("B-32-NOVO")));
    }

    @Test
    @DisplayName("Deve lançar DataIntegrityViolationException no banco ao salvar duas bombas com mesmo identificador normalizado")
    void deveLancarExcecaoIntegridadeBancoAoSalvarIdentificadoresNormalizadosIguais() {
        TipoCombustivel tipo = tipoCombustivelRepository.saveAndFlush(new TipoCombustivel("Combustivel Dup", new BigDecimal("5.000")));
        bombaRepository.saveAndFlush(new Bomba("B-DUP", tipo));

        assertThrows(DataIntegrityViolationException.class, () -> {
            bombaRepository.saveAndFlush(new Bomba("  b-dup ", tipo));
        });
    }
}
