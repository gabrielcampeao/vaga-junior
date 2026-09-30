package com.gabrielcampeao.posto.repository;

import com.gabrielcampeao.posto.domain.Abastecimento;
import com.gabrielcampeao.posto.web.dto.ResumoVendasResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AbastecimentoRepository extends JpaRepository<Abastecimento, Long>, JpaSpecificationExecutor<Abastecimento> {

    boolean existsByBombaId(Long bombaId);

    @Override
    @EntityGraph(attributePaths = {"bomba", "bomba.tipoCombustivel"})
    Page<Abastecimento> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"bomba", "bomba.tipoCombustivel"})
    Optional<Abastecimento> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"bomba", "bomba.tipoCombustivel"})
    Page<Abastecimento> findAll(Specification<Abastecimento> spec, Pageable pageable);

    @Query("SELECT new com.gabrielcampeao.posto.web.dto.ResumoVendasResponse(" +
           "tc.nome, SUM(a.litros), SUM(a.valorTotal)) " +
           "FROM Abastecimento a " +
           "JOIN a.bomba b " +
           "JOIN b.tipoCombustivel tc " +
           "WHERE a.dataHora BETWEEN :inicio AND :fim " +
           "GROUP BY tc.nome")
    List<ResumoVendasResponse> gerarResumoVendasPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
