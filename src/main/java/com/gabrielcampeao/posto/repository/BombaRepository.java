package com.gabrielcampeao.posto.repository;

import com.gabrielcampeao.posto.domain.Bomba;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BombaRepository extends JpaRepository<Bomba, Long> {

    boolean existsByIdentificadorNormalizado(String identificadorNormalizado);

    boolean existsByIdentificadorNormalizadoAndIdNot(String identificadorNormalizado, Long id);

    boolean existsByTipoCombustivelId(Long tipoCombustivelId);

    // EntityGraph carrega o TipoCombustivel junto na mesma query, resolvendo N+1 e mantendo open-in-view desativado
    @Override
    @EntityGraph(attributePaths = {"tipoCombustivel"})
    Page<Bomba> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"tipoCombustivel"})
    Optional<Bomba> findById(Long id);
}
