package com.gabrielcampeao.posto.config;

import com.gabrielcampeao.posto.domain.Bomba;
import com.gabrielcampeao.posto.domain.TipoCombustivel;
import com.gabrielcampeao.posto.repository.BombaRepository;
import com.gabrielcampeao.posto.repository.TipoCombustivelRepository;
import com.gabrielcampeao.posto.util.NormalizadorTexto;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Migração de bancos legados: preenche os campos {@code nomeNormalizado} e
 * {@code identificadorNormalizado} para registros criados antes da adição das
 * restrições de unicidade.
 *
 * <p>Quando dois registros legados possuem o mesmo valor normalizado (duplicata
 * preexistente), o segundo recebe um sufixo numérico incremental — ex.: {@code "dup (2)"}
 * — para que a constraint única possa ser satisfeita sem perda de dados.</p>
 */
@Component
public class MigracaoNomesNormalizados {

    private final TipoCombustivelRepository tipoCombustivelRepository;
    private final BombaRepository bombaRepository;

    public MigracaoNomesNormalizados(TipoCombustivelRepository tipoCombustivelRepository,
                                     BombaRepository bombaRepository) {
        this.tipoCombustivelRepository = tipoCombustivelRepository;
        this.bombaRepository = bombaRepository;
    }

    /**
     * Executa a migração de todos os registros pendentes (campo normalizado nulo).
     * Deve ser chamado antes de qualquer operação que dependa da unicidade normalizada.
     */
    @Transactional
    public void migrar() {
        migrarTiposCombustivel();
        migrarBombas();
    }

    private void migrarTiposCombustivel() {
        List<TipoCombustivel> pendentes = tipoCombustivelRepository.findAll()
                .stream()
                .filter(t -> t.getNomeNormalizado() == null && t.getNome() != null)
                .toList();

        for (TipoCombustivel tipo : pendentes) {
            String normalizado = NormalizadorTexto.normalizar(tipo.getNome());
            String normalizadoUnico = resolverConflito(normalizado,
                    n -> tipoCombustivelRepository.existsByNomeNormalizado(n));
            // setNome aciona o cálculo do nomeNormalizado via entidade,
            // mas precisamos garantir o valor desconflitado manualmente.
            tipo.setNome(tipo.getNome()); // preenche nomeNormalizado com valor base
            if (!normalizado.equals(normalizadoUnico)) {
                // há conflito: ajusta o nome para tornar o normalizado único
                tipo.setNome(tipo.getNome() + " " + sufixo(normalizadoUnico, normalizado));
            }
            tipoCombustivelRepository.save(tipo);
        }
    }

    private void migrarBombas() {
        List<Bomba> pendentes = bombaRepository.findAll()
                .stream()
                .filter(b -> b.getIdentificadorNormalizado() == null && b.getIdentificador() != null)
                .toList();

        for (Bomba bomba : pendentes) {
            String normalizado = NormalizadorTexto.normalizar(bomba.getIdentificador());
            String normalizadoUnico = resolverConflito(normalizado,
                    n -> bombaRepository.existsByIdentificadorNormalizado(n));
            bomba.setIdentificador(bomba.getIdentificador());
            if (!normalizado.equals(normalizadoUnico)) {
                bomba.setIdentificador(bomba.getIdentificador() + " " + sufixo(normalizadoUnico, normalizado));
            }
            bombaRepository.save(bomba);
        }
    }

    /**
     * Encontra o primeiro valor único adicionando sufixos {@code "(dup 2)"}, {@code "(dup 3)"}, ...
     *
     * @param base      valor normalizado desejado
     * @param existeCheck função que verifica se um normalizado já está em uso
     * @return valor único (pode ser igual a {@code base} se não houver conflito)
     */
    private String resolverConflito(String base, java.util.function.Predicate<String> existeCheck) {
        if (!existeCheck.test(base)) {
            return base;
        }
        int contador = 2;
        while (true) {
            String candidato = base + " (dup " + contador + ")";
            if (!existeCheck.test(candidato)) {
                return candidato;
            }
            contador++;
        }
    }

    /**
     * Extrai o sufixo de desambiguação do valor normalizado único em relação ao base.
     * Ex.: base="etanol", unico="etanol (dup 2)" → retorna "(dup 2)".
     */
    private String sufixo(String unico, String base) {
        return unico.substring(base.length()).trim();
    }
}
