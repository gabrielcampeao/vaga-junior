package com.gabrielcampeao.posto.repository;

import com.gabrielcampeao.posto.domain.TipoCombustivel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoCombustivelRepository extends JpaRepository<TipoCombustivel, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
