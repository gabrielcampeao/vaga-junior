package com.gabrielcampeao.posto.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabrielcampeao.posto.web.dto.TipoCombustivelRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiErrosIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar 400 ao enviar JSON malformado")
    void deveRetornar400QuandoJsonMalformado() throws Exception {
        mockMvc.perform(post("/api/tipos-combustivel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensagem", is("Corpo da requisição ausente ou com formato incorreto")));
    }

    @Test
    @DisplayName("Deve retornar 400 quando o parâmetro da URL for de tipo incompatível")
    void deveRetornar400QuandoTipoParametroInvalido() throws Exception {
        mockMvc.perform(get("/api/bombas/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("Deve retornar 400 quando o parâmetro de ordenação sort for inválido")
    void deveRetornar400QuandoSortInvalido() throws Exception {
        mockMvc.perform(get("/api/tipos-combustivel?sort=naoExiste"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("Deve retornar 400 com mapa de campos quando a validação falhar")
    void deveRetornar400ComMapaDeCamposNaValidacao() throws Exception {
        TipoCombustivelRequest request = new TipoCombustivelRequest("", null);

        mockMvc.perform(post("/api/tipos-combustivel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.campos", notNullValue()));
    }

    @Test
    @DisplayName("Deve retornar 415 quando Content-Type for application/x-www-form-urlencoded")
    void deveRetornar415QuandoMediaTypeNaoSuportado() throws Exception {
        mockMvc.perform(post("/api/tipos-combustivel")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("nome=Gasolina&precoLitro=5.000"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status", is(415)));
    }

    @Test
    @DisplayName("Deve retornar 405 e header Allow quando método HTTP não for suportado")
    void deveRetornar405QuandoMetodoHttpNaoSuportado() throws Exception {
        mockMvc.perform(patch("/api/tipos-combustivel/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.status", is(405)));
    }

    @Test
    @DisplayName("Deve retornar 404 quando recurso de URL não for encontrado")
    void deveRetornar404QuandoRecursoNaoEncontrado() throws Exception {
        mockMvc.perform(get("/api/naoexiste"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.mensagem", is("Recurso não encontrado: /api/naoexiste")));
    }
}
