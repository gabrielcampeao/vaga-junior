package com.gabrielcampeao.posto.repository;

import com.gabrielcampeao.posto.domain.TipoCombustivel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoCombustivelRepository extends JpaRepository<TipoCombustivel, Long> {

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);
}
