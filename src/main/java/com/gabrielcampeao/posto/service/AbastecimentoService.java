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
     * Se nenhuma data for fornecida, assume-se os últimos 30 dias.
     *
     * @param inicio Data inicial do período (opcional)
     * @param fim Data final do período (opcional)
     * @return Lista com o resumo de vendas por combustível
     * @throws RequisicaoInvalidaException se a data de início for posterior à data de fim
     */
    @Transactional(readOnly = true)
    public List<ResumoVendasResponse> obterResumoVendas(LocalDateTime inicio, LocalDateTime fim) {
        validarPeriodo(inicio, fim);
        LocalDateTime dataInicio = inicio != null ? inicio : LocalDateTime.now().minusDays(30);
        LocalDateTime dataFim = fim != null ? fim : LocalDateTime.now();
        return abastecimentoRepository.gerarResumoVendasPorPeriodo(dataInicio, dataFim);
    }

    @Transactional(readOnly = true)
    public Abastecimento buscarPorId(Long id) {
        return abastecimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Abastecimento não encontrado com id " + id));
    }

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

    private LitrosEValor calcularLitrosEValor(BigDecimal litros, BigDecimal valor, BigDecimal precoLitro) {
        LitrosEValor res;
        if (valor != null) {
            BigDecimal litrosCalculados = Abastecimento.calcularLitros(valor, precoLitro);
            res = new LitrosEValor(litrosCalculados, valor);
        } else {
            BigDecimal valorCalculado = Abastecimento.calcularValorTotal(litros, precoLitro);
            res = new LitrosEValor(litros, valorCalculado);
        }
        validarLimiteLitros(res.litros());
        return res;
    }

    private void validarLimiteLitros(BigDecimal litros) {
        if (limiteLitros != null && litros.compareTo(limiteLitros) > 0) {
            throw new LimiteLitrosExcedidoException(
                    "Quantidade de litros (" + litros + ") excede o limite máximo permitido por abastecimento (" + limiteLitros + " litros)");
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
