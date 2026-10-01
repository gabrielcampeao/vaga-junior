package com.gabrielcampeao.posto.service;

import com.gabrielcampeao.posto.domain.Abastecimento;
import com.gabrielcampeao.posto.domain.Bomba;
import com.gabrielcampeao.posto.repository.AbastecimentoRepository;
import com.gabrielcampeao.posto.repository.AbastecimentoSpecification;
import com.gabrielcampeao.posto.service.exception.LimiteLitrosExcedidoException;
import com.gabrielcampeao.posto.service.exception.RecursoNaoEncontradoException;
import com.gabrielcampeao.posto.service.exception.RequisicaoInvalidaException;
import com.gabrielcampeao.posto.web.dto.ResumoVendasResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AbastecimentoService {

    /**
     * Valor total máximo permitido para um abastecimento (R$ 99.999.999,99),
     * correspondente ao limite máximo suportado pela coluna NUMERIC(10,2) do banco de dados.
     */
    private static final BigDecimal VALOR_TOTAL_MAXIMO = new BigDecimal("99999999.99");

    private final AbastecimentoRepository abastecimentoRepository;
    private final BombaService bombaService;
    private final BigDecimal limiteLitros;

    public AbastecimentoService(AbastecimentoRepository abastecimentoRepository,
                                BombaService bombaService,
                                @Value("${posto.abastecimento.limite-litros:200.000}") BigDecimal limiteLitros) {
        this.abastecimentoRepository = abastecimentoRepository;
        this.bombaService = bombaService;
        this.limiteLitros = limiteLitros;
    }

    /**
     * Pesquisa abastecimentos aplicando filtros opcionais de bomba e período de datas.
     * O resultado é paginado e ordenado, por padrão, da data mais recente para a mais antiga.
     *
     * @param bombaId ID da bomba (opcional)
     * @param inicio Data inicial do período (opcional)
     * @param fim Data final do período (opcional)
     * @param pageable Informações de paginação
     * @return Página com os abastecimentos encontrados
     * @throws RequisicaoInvalidaException se a data de início for posterior à data de fim
     */
    @Transactional(readOnly = true)
    public Page<Abastecimento> pesquisar(Long bombaId, LocalDateTime inicio, LocalDateTime fim, Pageable pageable) {
        validarPeriodo(inicio, fim);

        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "dataHora");
        Pageable pageableOrdenado = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Specification<Abastecimento> spec = Specification
                .where(AbastecimentoSpecification.bombaIdIgual(bombaId))
                .and(AbastecimentoSpecification.dataHoraApartirDe(inicio))
                .and(AbastecimentoSpecification.dataHoraAte(fim));

        return abastecimentoRepository.findAll(spec, pageableOrdenado);
    }

    /**
     * Gera um resumo do total de vendas (em valor e litros) agrupado por tipo de combustível.
     * Regras para o período:
     * <ul>
     *   <li>Sem datas fornecidas: últimos 30 dias até a data/hora atual.</li>
     *   <li>Apenas data final (fim): período de 30 dias anteriores à data fim até a própria data fim.</li>
     *   <li>Apenas data inicial (inicio): período da data inicio até a data/hora atual.</li>
     *   <li>Ambas as datas fornecidas: intervalo exato entre a data inicio e a data fim.</li>
     * </ul>
     *
     * @param inicio Data inicial do período (opcional)
     * @param fim Data final do período (opcional)
     * @return Lista com o resumo de vendas por combustível
     * @throws RequisicaoInvalidaException se a data de início for posterior à data de fim
     */
    @Transactional(readOnly = true)
    public List<ResumoVendasResponse> obterResumoVendas(LocalDateTime inicio, LocalDateTime fim) {
        LocalDateTime dataFim = fim != null ? fim : LocalDateTime.now();
        LocalDateTime dataInicio = inicio != null ? inicio : dataFim.minusDays(30);
        validarPeriodo(dataInicio, dataFim);
        return abastecimentoRepository.gerarResumoVendasPorPeriodo(dataInicio, dataFim);
    }

    @Transactional(readOnly = true)
    public Abastecimento buscarPorId(Long id) {
        return abastecimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Abastecimento não encontrado com id " + id));
    }

    /**
     * Registra um novo abastecimento por litros ou por valor em reais.
     * O preço por litro aplicado é o valor vigente da bomba no momento do registro.
     */
    @Transactional
    public Abastecimento registrar(Long bombaId, BigDecimal litros, LocalDateTime dataHora, BigDecimal valor) {
        validarLitrosOuValor(litros, valor);

        Bomba bomba = bombaService.buscarPorId(bombaId);
        BigDecimal precoLitro = bomba.getTipoCombustivel().getPrecoLitro();

        LitrosEValor calculo = calcularLitrosEValor(litros, valor, precoLitro);

        LocalDateTime dataRegistro = dataHora != null ? dataHora : LocalDateTime.now();
        Abastecimento abastecimento = new Abastecimento(bomba, dataRegistro, calculo.litros(), precoLitro, calculo.valorTotal());
        return abastecimentoRepository.save(abastecimento);
    }

    /**
     * Atualiza um abastecimento existente.
     * Se a bomba não for alterada, preserva o preço por litro histórico registrado.
     * Se a bomba for alterada, assume o preço por litro vigente da nova bomba.
     */
    @Transactional
    public Abastecimento atualizar(Long id, Long bombaId, BigDecimal litros, LocalDateTime dataHora, BigDecimal valor) {
        validarLitrosOuValor(litros, valor);

        Abastecimento existente = buscarPorId(id);
        Bomba bomba = bombaService.buscarPorId(bombaId);

        boolean bombaMudou = !existente.getBomba().getId().equals(bomba.getId());
        BigDecimal precoLitro = bombaMudou ? bomba.getTipoCombustivel().getPrecoLitro() : existente.getPrecoLitro();

        LitrosEValor calculo = calcularLitrosEValor(litros, valor, precoLitro);

        existente.setBomba(bomba);
        existente.setLitros(calculo.litros());
        existente.setPrecoLitro(precoLitro);
        existente.setValorTotal(calculo.valorTotal());
        if (dataHora != null) {
            existente.setDataHora(dataHora);
        }

        return abastecimentoRepository.save(existente);
    }

    @Transactional
    public void excluir(Long id) {
        Abastecimento abastecimento = buscarPorId(id);
        abastecimentoRepository.delete(abastecimento);
    }

    private record LitrosEValor(BigDecimal litros, BigDecimal valorTotal) {}

    /**
     * Realiza o cálculo dos campos finais de litros e valor total.
     * No modo por valor, o valorTotal retornado é exatamente o valor fornecido e os litros são arredondados para 3 casas decimais (HALF_UP),
     * o que significa que a multiplicação (litros * precoLitro) pode diferir do valor informado em centavos.
     * A validação do limite máximo de litros é aplicada sempre sobre a quantidade final de litros calculada.
     */
    private LitrosEValor calcularLitrosEValor(BigDecimal litros, BigDecimal valor, BigDecimal precoLitro) {
        LitrosEValor res;
        if (valor != null) {
            BigDecimal litrosCalculados = Abastecimento.calcularLitros(valor, precoLitro);
            if (litrosCalculados.compareTo(BigDecimal.ZERO) == 0) {
                throw new RequisicaoInvalidaException("O valor informado é insuficiente para abastecer ao menos 0,001 litro");
            }
            res = new LitrosEValor(litrosCalculados, valor);
        } else {
            BigDecimal valorCalculado = Abastecimento.calcularValorTotal(litros, precoLitro);
            res = new LitrosEValor(litros, valorCalculado);
        }
        if (res.valorTotal().compareTo(VALOR_TOTAL_MAXIMO) > 0) {
            throw new RequisicaoInvalidaException("O valor total do abastecimento excede o máximo permitido");
        }
        validarLimiteLitros(res.litros());
        return res;
    }

    private void validarLimiteLitros(BigDecimal litros) {
        if (limiteLitros != null && litros.compareTo(limiteLitros) > 0) {
            String litrosStr = litros.stripTrailingZeros().toPlainString();
            String limiteStr = limiteLitros.stripTrailingZeros().toPlainString();
            throw new LimiteLitrosExcedidoException(
                    "Quantidade de litros (" + litrosStr + ") excede o limite máximo permitido por abastecimento (" + limiteStr + " litros)");
        }
    }

    private void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new RequisicaoInvalidaException("A data de início não pode ser posterior à data de fim");
        }
    }

    private void validarLitrosOuValor(BigDecimal litros, BigDecimal valor) {
        if (litros != null && valor != null) {
            throw new RequisicaoInvalidaException("Informe apenas litros ou valor, não ambos");
        }
        if (litros == null && valor == null) {
            throw new RequisicaoInvalidaException("Informe litros ou valor");
        }
    }
}
