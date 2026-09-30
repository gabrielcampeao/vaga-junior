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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), new BigDecimal("10.5"), null, null);

        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.litros", is(10.5)))
                .andExpect(jsonPath("$.precoLitro", is(6.0)))
                .andExpect(jsonPath("$.valorTotal", is(63.0)));
    }

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
    @DisplayName("Deve retornar abastecimento por id com bomba e tipo de combustível carregados")
    void deveRetornarAbastecimentoPorIdComBombaETipoCombustivel() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("GNV", new BigDecimal("3.500")));
        Bomba bomba = bombaRepository.save(new Bomba("B-10", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), new BigDecimal("5.000"), null, null);

        String location = mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bomba.identificador", is("B-10")))
                .andExpect(jsonPath("$.bomba.tipoCombustivel.nome", is("GNV")));
    }

    @Test
    @DisplayName("Deve listar abastecimentos com bomba e tipo de combustível carregados")
    void deveListarAbastecimentosComBombaETipoCombustivel() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Diesel S10", new BigDecimal("6.500")));
        Bomba bomba = bombaRepository.save(new Bomba("B-11", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), new BigDecimal("20.000"), null, null);

        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/abastecimentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].bomba.identificador", is("B-11")))
                .andExpect(jsonPath("$.content[0].bomba.tipoCombustivel.nome", is("Diesel S10")));
    }

    @Test
    @DisplayName("Deve filtrar abastecimentos combinando data e bomba, permitindo intervalos abertos")
    void deveFiltrarAbastecimentosComIntervalosAbertos() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Diesel S10", new BigDecimal("6.500")));
        Bomba bomba = bombaRepository.save(new Bomba("B-11", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), new BigDecimal("20.000"), null, null);

        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/abastecimentos?bombaId=" + bomba.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(get("/api/abastecimentos?inicio=2020-01-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(get("/api/abastecimentos?fim=2050-01-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("Deve retornar 400 se data de início for maior que data de fim no filtro")
    void deveRetornar400SeInicioMaiorQueFim() throws Exception {
        mockMvc.perform(get("/api/abastecimentos?inicio=2024-01-02T00:00:00&fim=2024-01-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensagem", is("A data de início não pode ser posterior à data de fim")));
    }

    @Test
    @DisplayName("Atualizar abastecimento não deve alterar o preço histórico se a bomba não mudar")
    void devePreservarPrecoHistoricoNaAtualizacao() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Etanol", new BigDecimal("3.500")));
        Bomba bomba = bombaRepository.save(new Bomba("B-03", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), new BigDecimal("10.0"), null, null);
        String location = mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        TipoCombustivelRequest requestUpdatePreco = new TipoCombustivelRequest("Etanol", new BigDecimal("4.000"));
        mockMvc.perform(put("/api/tipos-combustivel/" + tipo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUpdatePreco)))
                .andExpect(status().isOk());

        AbastecimentoRequest requestUpdateAbastecimento = new AbastecimentoRequest(bomba.getId(), new BigDecimal("10.0"), null, null);
        mockMvc.perform(put(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUpdateAbastecimento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precoLitro", is(3.5)))
                .andExpect(jsonPath("$.valorTotal", is(35.0)));
    }

    @Test
    @DisplayName("Deve retornar 400 se a quantidade de litros tiver mais de 3 casas decimais")
    void deveRetornar400SeLitrosTiverMaisDeTresCasasDecimais() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Diesel", new BigDecimal("6.000")));
        Bomba bomba = bombaRepository.save(new Bomba("B-20", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), new BigDecimal("10.1234"), null, null);

        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.campos.litros", is("A quantidade de litros deve ter até 7 dígitos inteiros e 3 casas decimais")));
    }

    @Test
    @DisplayName("Deve retornar 400 se a dataHora for no futuro")
    void deveRetornar400SeDataHoraFutura() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("GNV S", new BigDecimal("3.000")));
        Bomba bomba = bombaRepository.save(new Bomba("B-21", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), new BigDecimal("10.000"), LocalDateTime.now().plusDays(1), null);

        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.campos.dataHora", is("A data e hora não pode estar no futuro")));
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
    @DisplayName("Deve registrar abastecimento informando valor em reais e calcular litros automaticamente")
    void deveRegistrarAbastecimentoPorValor() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Gasolina V1", new BigDecimal("5.000")));
        Bomba bomba = bombaRepository.save(new Bomba("B-40", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), null, null, new BigDecimal("50.00"));

        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.litros", is(10.0)))
                .andExpect(jsonPath("$.precoLitro", is(5.0)))
                .andExpect(jsonPath("$.valorTotal", is(50.0)));
    }

    @Test
    @DisplayName("Deve retornar 400 ao informar litros e valor ao mesmo tempo")
    void deveRetornar400QuandoLitrosEValorInformadosJuntos() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Gasolina V2", new BigDecimal("5.000")));
        Bomba bomba = bombaRepository.save(new Bomba("B-41", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), new BigDecimal("10.000"), null, new BigDecimal("50.00"));

        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensagem", is("Informe apenas litros ou valor, não ambos")));
    }

    @Test
    @DisplayName("Deve retornar 400 quando valor resultar em litros acima do limite")
    void deveRetornar400QuandoValorResultaEmLitrosAcimaDoLimite() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("GNV V3", new BigDecimal("1.000")));
        Bomba bomba = bombaRepository.save(new Bomba("B-42", tipo));

        // valor 300 / preco 1.000 = 300 litros, acima do limite de 200
        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), null, null, new BigDecimal("300.00"));

        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("Deve retornar 400 quando nenhum campo litros e valor for informado")
    void deveRetornar400QuandoNemLitrosNemValorInformado() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Diesel V4", new BigDecimal("6.000")));
        Bomba bomba = bombaRepository.save(new Bomba("B-43", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), null, null, null);

        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensagem", is("Informe litros ou valor")));
    }

    @Test
    @DisplayName("Deve retornar 400 ao validar litros e valor mesmo se bomba não existir")
    void deveRetornar400AoValidarLitrosEValorMesmoComBombaInexistente() throws Exception {
        AbastecimentoRequest request = new AbastecimentoRequest(99999L, new BigDecimal("10.000"), null, new BigDecimal("50.00"));

        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensagem", is("Informe apenas litros ou valor, não ambos")));
    }

    @Test
    @DisplayName("Deve retornar 400 quando valor resultar em zero litros")
    void deveRetornar400QuandoValorResultarEmZeroLitros() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Combustivel Caro", new BigDecimal("100.000")));
        Bomba bomba = bombaRepository.save(new Bomba("B-50", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), null, null, new BigDecimal("0.01"));

        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensagem", is("O valor informado é insuficiente para abastecer ao menos 0,001 litro")));
    }

    @Test
    @DisplayName("Deve atualizar abastecimento por valor mantendo preço histórico quando preço do combustível muda")
    void deveAtualizarAbastecimentoPorValorMantendoPrecoHistorico() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Gasolina H1", new BigDecimal("5.000")));
        Bomba bomba = bombaRepository.save(new Bomba("B-60", tipo));

        AbastecimentoRequest postReq = new AbastecimentoRequest(bomba.getId(), new BigDecimal("10.000"), null, null);
        String responseStr = mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long abastecimentoId = objectMapper.readTree(responseStr).get("id").asLong();

        TipoCombustivelRequest tipoUpdate = new TipoCombustivelRequest("Gasolina H1", new BigDecimal("6.000"));
        mockMvc.perform(put("/api/tipos-combustivel/" + tipo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipoUpdate)))
                .andExpect(status().isOk());

        AbastecimentoRequest putReq = new AbastecimentoRequest(bomba.getId(), null, null, new BigDecimal("50.00"));
        mockMvc.perform(put("/api/abastecimentos/" + abastecimentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.litros", is(10.0)))
                .andExpect(jsonPath("$.precoLitro", is(5.0)))
                .andExpect(jsonPath("$.valorTotal", is(50.0)));
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
    @DisplayName("Deve retornar 405 e header Allow quando metodo HTTP nao for suportado")
    void deveRetornar405QuandoMetodoHttpNaoSuportado() throws Exception {
        mockMvc.perform(patch("/api/tipos-combustivel/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.status", is(405)));
    }

    @Test
    @DisplayName("Deve retornar 404 quando recurso de URL nao for encontrado")
    void deveRetornar404QuandoRecursoNaoEncontrado() throws Exception {
        mockMvc.perform(get("/api/naoexiste"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.mensagem", is("Recurso não encontrado: /api/naoexiste")));
    }

    @Test
    @DisplayName("Deve formatar o limite de litros na mensagem de erro sem decimais desnecessarios")
    void deveFormatarLimiteDeLitrosNaMensagemDeErro() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Gasolina L1", new BigDecimal("5.000")));
        Bomba bomba = bombaRepository.save(new Bomba("B-70", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), new BigDecimal("200.001"), null, null);

        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensagem", containsString("(200 litros)")));
    }

    @Test
    @DisplayName("Deve filtrar resumo de vendas informando apenas data fim (janela de 30 dias anteriores a fim)")
    void deveFiltrarResumoVendasApenasDataFim() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Resumo R1", new BigDecimal("5.000")));
        Bomba bomba = bombaRepository.save(new Bomba("B-80", tipo));

        LocalDateTime dataDez = LocalDateTime.of(2025, 12, 15, 10, 0, 0);
        AbastecimentoRequest reqDez = new AbastecimentoRequest(bomba.getId(), new BigDecimal("10.000"), dataDez, null);
        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDez)))
                .andExpect(status().isCreated());

        LocalDateTime dataJun = LocalDateTime.of(2025, 6, 15, 10, 0, 0);
        AbastecimentoRequest reqJun = new AbastecimentoRequest(bomba.getId(), new BigDecimal("15.000"), dataJun, null);
        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqJun)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/abastecimentos/resumo?fim=2025-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipoCombustivel", is("Resumo R1")))
                .andExpect(jsonPath("$[0].totalLitros", is(10.0)));
    }

    @Test
    @DisplayName("Deve filtrar resumo de vendas informando apenas data inicio")
    void deveFiltrarResumoVendasApenasDataInicio() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Resumo R2", new BigDecimal("4.500")));
        Bomba bomba = bombaRepository.save(new Bomba("B-81", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), new BigDecimal("20.000"), null, null);
        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        String ontemIso = LocalDateTime.now().minusDays(1).toString();
        mockMvc.perform(get("/api/abastecimentos/resumo?inicio=" + ontemIso))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.tipoCombustivel == 'Resumo R2')]", hasSize(1)));
    }

    @Test
    @DisplayName("Deve filtrar resumo de vendas com periodo completo inicio e fim")
    void deveFiltrarResumoVendasPeriodoCompleto() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Resumo R3", new BigDecimal("6.000")));
        Bomba bomba = bombaRepository.save(new Bomba("B-82", tipo));

        LocalDateTime dataDez = LocalDateTime.of(2025, 12, 15, 10, 0, 0);
        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), new BigDecimal("10.000"), dataDez, null);
        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/abastecimentos/resumo?inicio=2025-12-01T00:00:00&fim=2025-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipoCombustivel", is("Resumo R3")))
                .andExpect(jsonPath("$[0].totalLitros", is(10.0)));
    }
}
