package com.gabrielcampeao.posto.repository;

import com.gabrielcampeao.posto.domain.Abastecimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AbastecimentoRepository extends JpaRepository<Abastecimento, Long> {

    boolean existsByBombaId(Long bombaId);

    @EntityGraph(attributePaths = {"bomba", "bomba.tipoCombustivel"})
    Page<Abastecimento> findByBombaIdAndDataHoraBetween(Long bombaId, LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    @EntityGraph(attributePaths = {"bomba", "bomba.tipoCombustivel"})
    Page<Abastecimento> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    @EntityGraph(attributePaths = {"bomba", "bomba.tipoCombustivel"})
    Page<Abastecimento> findByBombaId(Long bombaId, Pageable pageable);
}
