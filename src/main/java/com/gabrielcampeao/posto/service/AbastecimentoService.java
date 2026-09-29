package com.gabrielcampeao.posto.service;

import com.gabrielcampeao.posto.domain.Abastecimento;
import com.gabrielcampeao.posto.domain.Bomba;
import com.gabrielcampeao.posto.repository.AbastecimentoRepository;
import com.gabrielcampeao.posto.service.exception.LimiteLitrosExcedidoException;
import com.gabrielcampeao.posto.service.exception.RecursoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

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

    public Page<Abastecimento> pesquisar(Long bombaId, LocalDateTime inicio, LocalDateTime fim, Pageable pageable) {
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "dataHora");
        Pageable pageableOrdenado = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        if (bombaId != null && inicio != null && fim != null) {
            return abastecimentoRepository.findByBombaIdAndDataHoraBetween(bombaId, inicio, fim, pageableOrdenado);
        } else if (bombaId != null) {
            return abastecimentoRepository.findByBombaId(bombaId, pageableOrdenado);
        } else if (inicio != null && fim != null) {
            return abastecimentoRepository.findByDataHoraBetween(inicio, fim, pageableOrdenado);
        }

        return abastecimentoRepository.findAll(pageableOrdenado);
    }

    public Abastecimento buscarPorId(Long id) {
        return abastecimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Abastecimento não encontrado com id " + id));
    }

    @Transactional
    public Abastecimento registrar(Long bombaId, BigDecimal litros, LocalDateTime dataHora) {
        validarLimiteLitros(litros);

        Bomba bomba = bombaService.buscarPorId(bombaId);
        BigDecimal precoLitro = bomba.getTipoCombustivel().getPrecoLitro();
        BigDecimal valorTotal = litros.multiply(precoLitro).setScale(2, RoundingMode.HALF_UP);
        LocalDateTime dataRegistro = dataHora != null ? dataHora : LocalDateTime.now();

        Abastecimento abastecimento = new Abastecimento(bomba, dataRegistro, litros, precoLitro, valorTotal);
        return abastecimentoRepository.save(abastecimento);
    }

    @Transactional
    public Abastecimento atualizar(Long id, Long bombaId, BigDecimal litros, LocalDateTime dataHora) {
        validarLimiteLitros(litros);

        Abastecimento existente = buscarPorId(id);
        Bomba bomba = bombaService.buscarPorId(bombaId);

        BigDecimal precoLitro = bomba.getTipoCombustivel().getPrecoLitro();
        BigDecimal valorTotal = litros.multiply(precoLitro).setScale(2, RoundingMode.HALF_UP);

        existente.setBomba(bomba);
        existente.setLitros(litros);
        existente.setPrecoLitro(precoLitro);
        existente.setValorTotal(valorTotal);
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

    private void validarLimiteLitros(BigDecimal litros) {
        if (limiteLitros != null && litros.compareTo(limiteLitros) > 0) {
            throw new LimiteLitrosExcedidoException(
                    "Quantidade de litros (" + litros + ") excede o limite máximo permitido por abastecimento (" + limiteLitros + " litros)");
        }
    }
}
