package com.gabrielcampeao.posto.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabrielcampeao.posto.domain.Bomba;
import com.gabrielcampeao.posto.domain.TipoCombustivel;
import com.gabrielcampeao.posto.repository.AbastecimentoRepository;
import com.gabrielcampeao.posto.repository.BombaRepository;
import com.gabrielcampeao.posto.repository.TipoCombustivelRepository;
import com.gabrielcampeao.posto.web.dto.TipoCombustivelRequest;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TipoCombustivelControllerTest {

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
    @DisplayName("Deve cadastrar tipo de combustível com sucesso e retornar 201 com Location")
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
    @DisplayName("Deve proibir exclusão de combustível vinculado a bomba")
    void deveProibirExclusaoDeCombustivelEmUso() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Diesel", new BigDecimal("6.199")));
        bombaRepository.save(new Bomba("B-01", tipo));

        mockMvc.perform(delete("/api/tipos-combustivel/" + tipo.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("Deve retornar 409 ao tentar cadastrar combustível com nome duplicado ignorando maiúsculas e minúsculas")
    void deveRetornar409QuandoNomeCombustivelDuplicadoIgnoreCase() throws Exception {
        tipoCombustivelRepository.save(new TipoCombustivel("Etanol", new BigDecimal("4.099")));

        TipoCombustivelRequest request = new TipoCombustivelRequest("etanol", new BigDecimal("4.199"));

        mockMvc.perform(post("/api/tipos-combustivel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.erro", is("Conflito")));
    }

    @Test
    @DisplayName("Deve retornar 409 ao tentar cadastrar combustível com nome duplicado ignorando espaços extras")
    void deveRetornar409QuandoNomeCombustivelDuplicadoComEspacos() throws Exception {
        tipoCombustivelRepository.save(new TipoCombustivel("Etanol Aditivado", new BigDecimal("4.099")));

        TipoCombustivelRequest request = new TipoCombustivelRequest(" Etanol Aditivado ", new BigDecimal("4.199"));

        mockMvc.perform(post("/api/tipos-combustivel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.erro", is("Conflito")));
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar cadastrar combustível com precoLitro excedendo 3 dígitos inteiros")
    void deveRetornar400QuandoPrecoLitroExcederTresDigitosInteiros() throws Exception {
        TipoCombustivelRequest request = new TipoCombustivelRequest("Combustivel Super Caro", new BigDecimal("1000.000"));

        mockMvc.perform(post("/api/tipos-combustivel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.campos.precoLitro", is("O preço por litro deve ter até 3 dígitos inteiros e 3 casas decimais")));
    }

    @Test
    @DisplayName("Deve lançar DataIntegrityViolationException no banco ao salvar dois combustíveis com mesmo nome normalizado")
    void deveLancarExcecaoIntegridadeBancoAoSalvarNomesNormalizadosIguais() {
        tipoCombustivelRepository.saveAndFlush(new TipoCombustivel("Duplo", new BigDecimal("5.000")));

        assertThrows(DataIntegrityViolationException.class, () -> {
            tipoCombustivelRepository.saveAndFlush(new TipoCombustivel("  duplo ", new BigDecimal("5.500")));
        });
    }
}
