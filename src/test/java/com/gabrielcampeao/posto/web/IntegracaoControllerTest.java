package com.gabrielcampeao.posto.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabrielcampeao.posto.domain.Bomba;
import com.gabrielcampeao.posto.domain.TipoCombustivel;
import com.gabrielcampeao.posto.repository.AbastecimentoRepository;
import com.gabrielcampeao.posto.repository.BombaRepository;
import com.gabrielcampeao.posto.repository.TipoCombustivelRepository;
import com.gabrielcampeao.posto.web.dto.AbastecimentoRequest;
import com.gabrielcampeao.posto.web.dto.BombaRequest;
import com.gabrielcampeao.posto.web.dto.TipoCombustivelRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IntegracaoControllerTest {

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
    @DisplayName("Deve cadastrar tipo de combustivel com sucesso e retornar 201 com Location")
    void deveCadastrarTipoCombustivel() throws Exception {
        TipoCombustivelRequest request = new TipoCombustivelRequest("Gasolina Comum", new BigDecimal("5.899"));

        mockMvc.perform(post("/api/tipos-combustivel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Gasolina Comum")))
                .andExpect(jsonPath("$.precoLitro", is(5.899)));
    }

    @Test
    @DisplayName("Deve retornar 409 ao tentar cadastrar combustível com nome duplicado")
    void deveRetornar409QuandoNomeCombustivelDuplicado() throws Exception {
        tipoCombustivelRepository.save(new TipoCombustivel("Etanol", new BigDecimal("4.099")));

        TipoCombustivelRequest request = new TipoCombustivelRequest("Etanol", new BigDecimal("4.199"));

        mockMvc.perform(post("/api/tipos-combustivel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.erro", is("Conflito")));
    }

    @Test
    @DisplayName("Deve proibir exclusao de combustível vinculado a bomba")
    void deveProibirExclusaoDeCombustivelEmUso() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Diesel", new BigDecimal("6.199")));
        bombaRepository.save(new Bomba("B-01", tipo));

        mockMvc.perform(delete("/api/tipos-combustivel/" + tipo.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("Deve registrar abastecimento e calcular o valor total corretamente no servidor")
    void deveRegistrarAbastecimentoECalcularValorTotal() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Gasolina Aditivada", new BigDecimal("6.000")));
        Bomba bomba = bombaRepository.save(new Bomba("B-02", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), new BigDecimal("10.5"), null);

        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.litros", is(10.5)))
                .andExpect(jsonPath("$.precoLitro", is(6.0)))
                .andExpect(jsonPath("$.valorTotal", is(63.0)));
    }
}
