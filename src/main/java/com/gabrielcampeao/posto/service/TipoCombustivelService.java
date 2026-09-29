package com.gabrielcampeao.posto.service;

import com.gabrielcampeao.posto.domain.TipoCombustivel;
import com.gabrielcampeao.posto.repository.TipoCombustivelRepository;
import com.gabrielcampeao.posto.service.exception.NomeDuplicadoException;
import com.gabrielcampeao.posto.service.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TipoCombustivelService {

    private final TipoCombustivelRepository tipoCombustivelRepository;

    public TipoCombustivelService(TipoCombustivelRepository tipoCombustivelRepository) {
        this.tipoCombustivelRepository = tipoCombustivelRepository;
    }

    public Page<TipoCombustivel> listar(Pageable pageable) {
        return tipoCombustivelRepository.findAll(pageable);
    }

    public TipoCombustivel buscarPorId(Long id) {
        return tipoCombustivelRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Tipo de combustível não encontrado com id " + id));
    }

    @Transactional
    public TipoCombustivel criar(TipoCombustivel tipo) {
        validarNomeDuplicado(tipo.getNome(), null);
        return tipoCombustivelRepository.save(tipo);
    }

    @Transactional
    public TipoCombustivel atualizar(Long id, TipoCombustivel dados) {
        TipoCombustivel existente = buscarPorId(id);
        validarNomeDuplicado(dados.getNome(), id);
        existente.setNome(dados.getNome());
        existente.setPrecoLitro(dados.getPrecoLitro());
        return tipoCombustivelRepository.save(existente);
    }

    @Transactional
    public void excluir(Long id) {
        TipoCombustivel tipo = buscarPorId(id);
        tipoCombustivelRepository.delete(tipo);
    }

    private void validarNomeDuplicado(String nome, Long idIgnorar) {
        boolean duplicado = idIgnorar == null
                ? tipoCombustivelRepository.existsByNome(nome)
                : tipoCombustivelRepository.existsByNomeAndIdNot(nome, idIgnorar);

        if (duplicado) {
            throw new NomeDuplicadoException(
                    "Já existe um tipo de combustível com o nome \"" + nome + "\"");
        }
    }
}
