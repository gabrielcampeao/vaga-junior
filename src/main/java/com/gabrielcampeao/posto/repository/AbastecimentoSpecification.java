package com.gabrielcampeao.posto.repository;

import com.gabrielcampeao.posto.domain.Abastecimento;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class AbastecimentoSpecification {

    private AbastecimentoSpecification() {
    }

    public static Specification<Abastecimento> bombaIdIgual(Long bombaId) {
        return (root, query, cb) -> bombaId == null ? null : cb.equal(root.get("bomba").get("id"), bombaId);
    }

    public static Specification<Abastecimento> dataHoraApartirDe(LocalDateTime inicio) {
        return (root, query, cb) -> inicio == null ? null : cb.greaterThanOrEqualTo(root.get("dataHora"), inicio);
    }

    public static Specification<Abastecimento> dataHoraAte(LocalDateTime fim) {
        return (root, query, cb) -> fim == null ? null : cb.lessThanOrEqualTo(root.get("dataHora"), fim);
    }
}
