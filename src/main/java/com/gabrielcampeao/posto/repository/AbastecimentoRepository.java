package com.gabrielcampeao.posto.repository;

import com.gabrielcampeao.posto.domain.Abastecimento;
import com.gabrielcampeao.posto.web.dto.ResumoVendasResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AbastecimentoRepository extends JpaRepository<Abastecimento, Long> {

    boolean existsByBombaId(Long bombaId);

    @EntityGraph(attributePaths = {"bomba", "bomba.tipoCombustivel"})
    Page<Abastecimento> findByBombaIdAndDataHoraBetween(Long bombaId, LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    @EntityGraph(attributePaths = {"bomba", "bomba.tipoCombustivel"})
    Page<Abastecimento> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    @EntityGraph(attributePaths = {"bomba", "bomba.tipoCombustivel"})
    Page<Abastecimento> findByBombaId(Long bombaId, Pageable pageable);

    @Query("SELECT new com.gabrielcampeao.posto.web.dto.ResumoVendasResponse(" +
           "tc.nome, SUM(a.litros), SUM(a.valorTotal)) " +
           "FROM Abastecimento a " +
           "JOIN a.bomba b " +
           "JOIN b.tipoCombustivel tc " +
           "WHERE a.dataHora BETWEEN :inicio AND :fim " +
           "GROUP BY tc.nome")
    List<ResumoVendasResponse> gerarResumoVendasPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
