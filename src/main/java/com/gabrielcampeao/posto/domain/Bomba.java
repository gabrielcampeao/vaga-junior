package com.gabrielcampeao.posto.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "bomba")
public class Bomba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String identificador;

    @Column(name = "identificador_normalizado", unique = true)
    private String identificadorNormalizado;

    // Relacionamento LAZY para evitar a busca desnecessaria do combustível em todas as consultas
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_combustivel_id", nullable = false)
    private TipoCombustivel tipoCombustivel;

    public Bomba() {
    }

    public Bomba(String identificador, TipoCombustivel tipoCombustivel) {
        setIdentificador(identificador);
        this.tipoCombustivel = tipoCombustivel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
        this.identificadorNormalizado = normalizar(identificador);
    }

    public String getIdentificadorNormalizado() {
        return identificadorNormalizado;
    }

    private static String normalizar(String text) {
        if (text == null) {
            return null;
        }
        return text.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }

    public TipoCombustivel getTipoCombustivel() {
        return tipoCombustivel;
    }

    public void setTipoCombustivel(TipoCombustivel tipoCombustivel) {
        this.tipoCombustivel = tipoCombustivel;
    }
}
