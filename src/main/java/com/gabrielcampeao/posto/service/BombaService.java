package com.gabrielcampeao.posto.service;

import com.gabrielcampeao.posto.domain.Bomba;
import com.gabrielcampeao.posto.domain.TipoCombustivel;
import com.gabrielcampeao.posto.repository.AbastecimentoRepository;
import com.gabrielcampeao.posto.repository.BombaRepository;
import com.gabrielcampeao.posto.service.exception.NomeDuplicadoException;
import com.gabrielcampeao.posto.service.exception.RecursoEmUsoException;
import com.gabrielcampeao.posto.service.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BombaService {

    private final BombaRepository bombaRepository;
    private final TipoCombustivelService tipoCombustivelService;
    private final AbastecimentoRepository abastecimentoRepository;

    public BombaService(BombaRepository bombaRepository,
                        TipoCombustivelService tipoCombustivelService,
                        AbastecimentoRepository abastecimentoRepository) {
        this.bombaRepository = bombaRepository;
        this.tipoCombustivelService = tipoCombustivelService;
        this.abastecimentoRepository = abastecimentoRepository;
    }

    @Transactional(readOnly = true)
    public Page<Bomba> listar(Pageable pageable) {
        return bombaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Bomba buscarPorId(Long id) {
        return bombaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Bomba não encontrada com id " + id));
    }

    @Transactional
    public Bomba criar(String identificador, Long tipoCombustivelId) {
        String idNormalizado = identificador.trim();
        validarIdentificadorDuplicado(idNormalizado, null);
        TipoCombustivel tipo = tipoCombustivelService.buscarPorId(tipoCombustivelId);
        Bomba bomba = new Bomba(idNormalizado, tipo);
        return bombaRepository.save(bomba);
    }

    @Transactional
    public Bomba atualizar(Long id, String identificador, Long tipoCombustivelId) {
        Bomba existente = buscarPorId(id);
        String idNormalizado = identificador.trim();
        validarIdentificadorDuplicado(idNormalizado, id);
        TipoCombustivel tipo = tipoCombustivelService.buscarPorId(tipoCombustivelId);

        boolean combustivelMudou = !existente.getTipoCombustivel().getId().equals(tipo.getId());
        if (combustivelMudou && abastecimentoRepository.existsByBombaId(id)) {
            throw new RecursoEmUsoException(
                    "Não é possível trocar o combustível da bomba \""
                    + existente.getIdentificador()
                    + "\" porque existem abastecimentos registrados nela");
        }

        existente.setIdentificador(idNormalizado);
        existente.setTipoCombustivel(tipo);
        return bombaRepository.save(existente);
    }

    @Transactional
    public void excluir(Long id) {
        Bomba bomba = buscarPorId(id);

        if (abastecimentoRepository.existsByBombaId(bomba.getId())) {
            throw new RecursoEmUsoException(
                    "Não é possível excluir a bomba \"" + bomba.getIdentificador()
                    + "\" porque existem abastecimentos registrados nela");
        }

        bombaRepository.delete(bomba);
    }

    private void validarIdentificadorDuplicado(String identificador, Long idIgnorar) {
        boolean duplicado = idIgnorar == null
                ? bombaRepository.existsByIdentificadorIgnoreCase(identificador)
                : bombaRepository.existsByIdentificadorIgnoreCaseAndIdNot(identificador, idIgnorar);

        if (duplicado) {
            throw new NomeDuplicadoException(
                    "Já existe uma bomba com o identificador \"" + identificador + "\"");
        }
    }
}
