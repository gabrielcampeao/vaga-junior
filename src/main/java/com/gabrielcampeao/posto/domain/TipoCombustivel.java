package com.gabrielcampeao.posto.domain;

import com.gabrielcampeao.posto.util.NormalizadorTexto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Entity
@Table(name = "tipo_combustivel")
public class TipoCombustivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String nome;

    @Column(name = "nome_normalizado", unique = true)
    private String nomeNormalizado;

    // Três casas decimais porque os preços de combustível no Brasil usam a terceira casa
    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal precoLitro;

    public TipoCombustivel() {
    }

    public TipoCombustivel(String nome, BigDecimal precoLitro) {
        setNome(nome);
        setPrecoLitro(precoLitro);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
        this.nomeNormalizado = NormalizadorTexto.normalizar(nome);
    }

    public String getNomeNormalizado() {
        return nomeNormalizado;
    }

    public BigDecimal getPrecoLitro() {
        return precoLitro;
    }

    public void setPrecoLitro(BigDecimal precoLitro) {
        this.precoLitro = precoLitro != null ? precoLitro.setScale(3, java.math.RoundingMode.HALF_UP) : null;
    }
}
