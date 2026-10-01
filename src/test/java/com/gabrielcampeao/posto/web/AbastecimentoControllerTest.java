package com.gabrielcampeao.posto.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabrielcampeao.posto.domain.Bomba;
import com.gabrielcampeao.posto.domain.TipoCombustivel;
import com.gabrielcampeao.posto.repository.AbastecimentoRepository;
import com.gabrielcampeao.posto.repository.BombaRepository;
import com.gabrielcampeao.posto.repository.TipoCombustivelRepository;
import com.gabrielcampeao.posto.web.dto.AbastecimentoRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AbastecimentoControllerTest {

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
    @DisplayName("Deve formatar o limite de litros na mensagem de erro sem decimais desnecessários")
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
    @DisplayName("Deve filtrar resumo de vendas informando apenas data início")
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
    @DisplayName("Deve filtrar resumo de vendas com período completo início e fim")
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

    @Test
    @DisplayName("Deve padronizar escala decimal de litros, preco_litro e valorTotal no JSON de resposta")
    void devePadronizarEscalasDecimaisNoJsonDeResposta() throws Exception {
        TipoCombustivelRequest tipoReq = new TipoCombustivelRequest("Gasolina Escala", new BigDecimal("5"));
        String resTipo = mockMvc.perform(post("/api/tipos-combustivel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipoReq)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"precoLitro\":5.000")))
                .andReturn().getResponse().getContentAsString();

        Long tipoId = objectMapper.readTree(resTipo).get("id").asLong();
        Bomba bomba = bombaRepository.save(new Bomba("B-90", tipoCombustivelRepository.findById(tipoId).orElseThrow()));

        AbastecimentoRequest abLitrosReq = new AbastecimentoRequest(bomba.getId(), new BigDecimal("10"), null, null);
        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(abLitrosReq)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"litros\":10.000")));

        AbastecimentoRequest abValorReq = new AbastecimentoRequest(bomba.getId(), null, null, new BigDecimal("50"));
        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(abValorReq)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"valorTotal\":50.00")));
    }

    @Test
    @DisplayName("Deve retornar 400 no resumo quando início futuro sem fim resulta em período invertido")
    void deveRetornar400NoResumoQuandoInicioFuturoSemFim() throws Exception {
        mockMvc.perform(get("/api/abastecimentos/resumo?inicio=2030-01-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensagem", is("A data de início não pode ser posterior à data de fim")));
    }

    @Test
    @DisplayName("Deve retornar a mesma dataHora entre POST sem data e GET por id (truncada em segundos)")
    void deveRetornarMesmaDataHoraEntrePostSemDataEGetPorId() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Gasolina DH", new BigDecimal("5.000")));
        Bomba bomba = bombaRepository.save(new Bomba("B-DH", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), new BigDecimal("10.000"), null, null);

        String postResponse = mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String dataHoraPost = objectMapper.readTree(postResponse).get("dataHora").asText();

        // Use the id from the first POST
        Long id = objectMapper.readTree(postResponse).get("id").asLong();

        String getResponse = mockMvc.perform(get("/api/abastecimentos/" + id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String dataHoraGet = objectMapper.readTree(getResponse).get("dataHora").asText();

        org.junit.jupiter.api.Assertions.assertEquals(dataHoraPost, dataHoraGet,
                "dataHora do POST e GET devem ser idênticas (truncadas em segundos)");
    }

    @Test
    @DisplayName("Deve retornar resumo de vendas com período padrão de 30 dias quando nenhuma data for informada")
    void deveRetornarResumoComPeriodoPadraoDe30DiasQuandoSemDatas() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Resumo Default", new BigDecimal("5.000")));
        Bomba bomba = bombaRepository.save(new Bomba("B-DF", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), new BigDecimal("10.000"), null, null);
        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/abastecimentos/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.tipoCombustivel == 'Resumo Default')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.tipoCombustivel == 'Resumo Default')].totalLitros", is(java.util.List.of(10.0))));
    }

    @Test
    @DisplayName("Deve retornar 400 ao registrar abastecimento com quantidade de litros que resulte em valor inferior a R$ 0,01")
    void deveRetornar400AoRegistrarAbastecimentoComValorAbaixoMinimo() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Etanol Barato", new BigDecimal("4.099")));
        Bomba bomba = bombaRepository.save(new Bomba("B-MIN", tipo));

        AbastecimentoRequest request = new AbastecimentoRequest(bomba.getId(), new BigDecimal("0.001"), null, null);

        mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensagem", is("A quantidade de litros informada resulta em valor total inferior a R$ 0,01")));
    }

    @Test
    @DisplayName("Deve retornar 400 ao atualizar abastecimento com quantidade de litros que resulte em valor inferior a R$ 0,01")
    void deveRetornar400AoAtualizarAbastecimentoComValorAbaixoMinimo() throws Exception {
        TipoCombustivel tipo = tipoCombustivelRepository.save(new TipoCombustivel("Gasolina Barata", new BigDecimal("4.099")));
        Bomba bomba = bombaRepository.save(new Bomba("B-MIN-UPD", tipo));
        
        AbastecimentoRequest initialRequest = new AbastecimentoRequest(bomba.getId(), new BigDecimal("10.000"), null, null);
        String postResponse = mockMvc.perform(post("/api/abastecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(postResponse).get("id").asLong();

        AbastecimentoRequest updateRequest = new AbastecimentoRequest(bomba.getId(), new BigDecimal("0.001"), null, null);

        mockMvc.perform(put("/api/abastecimentos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensagem", is("A quantidade de litros informada resulta em valor total inferior a R$ 0,01")));
    }
}
