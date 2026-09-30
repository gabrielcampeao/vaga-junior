package com.gabrielcampeao.posto.service;

import com.gabrielcampeao.posto.domain.TipoCombustivel;
import com.gabrielcampeao.posto.repository.BombaRepository;
import com.gabrielcampeao.posto.repository.TipoCombustivelRepository;
import com.gabrielcampeao.posto.service.exception.NomeDuplicadoException;
import com.gabrielcampeao.posto.service.exception.RecursoEmUsoException;
import com.gabrielcampeao.posto.service.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TipoCombustivelService {

    private final TipoCombustivelRepository tipoCombustivelRepository;
    private final BombaRepository bombaRepository;

    public TipoCombustivelService(TipoCombustivelRepository tipoCombustivelRepository,
                                  BombaRepository bombaRepository) {
        this.tipoCombustivelRepository = tipoCombustivelRepository;
        this.bombaRepository = bombaRepository;
    }

    @Transactional(readOnly = true)
    public Page<TipoCombustivel> listar(Pageable pageable) {
        return tipoCombustivelRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public TipoCombustivel buscarPorId(Long id) {
        return tipoCombustivelRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Tipo de combustível não encontrado com id " + id));
    }

    @Transactional
    public TipoCombustivel criar(TipoCombustivel tipo) {
        tipo.setNome(tipo.getNome().trim());
        validarNomeDuplicado(tipo.getNome(), null);
        return tipoCombustivelRepository.save(tipo);
    }

    @Transactional
    public TipoCombustivel atualizar(Long id, TipoCombustivel dados) {
        TipoCombustivel existente = buscarPorId(id);
        String nomeNormalizado = dados.getNome().trim();
        validarNomeDuplicado(nomeNormalizado, id);
        existente.setNome(nomeNormalizado);
        existente.setPrecoLitro(dados.getPrecoLitro());
        return tipoCombustivelRepository.save(existente);
    }

    @Transactional
    public void excluir(Long id) {
        TipoCombustivel tipo = buscarPorId(id);

        if (bombaRepository.existsByTipoCombustivelId(tipo.getId())) {
            throw new RecursoEmUsoException(
                    "Não é possível excluir o combustível \"" + tipo.getNome()
                    + "\" porque existem bombas cadastradas com esse tipo");
        }

        tipoCombustivelRepository.delete(tipo);
    }

    private void validarNomeDuplicado(String nome, Long idIgnorar) {
        boolean duplicado = idIgnorar == null
                ? tipoCombustivelRepository.existsByNomeIgnoreCase(nome)
                : tipoCombustivelRepository.existsByNomeIgnoreCaseAndIdNot(nome, idIgnorar);

        if (duplicado) {
            throw new NomeDuplicadoException(
                    "Já existe um tipo de combustível com o nome \"" + nome + "\"");
        }
    }
}
