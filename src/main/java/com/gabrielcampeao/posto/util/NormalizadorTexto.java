package com.gabrielcampeao.posto.util;

/**
 * Utilitário para normalização de texto utilizado na comparação de unicidade
 * de nomes e identificadores, garantindo que diferenças de maiúsculas/minúsculas
 * e espaços extras não gerem duplicatas no banco de dados.
 */
public final class NormalizadorTexto {

    private NormalizadorTexto() {
        // Classe utilitária — não instanciar
    }

    /**
     * Normaliza um texto removendo espaços das extremidades, colapsando espaços
     * internos consecutivos em um único e convertendo para minúsculas.
     *
     * @param texto texto a ser normalizado; pode ser {@code null}
     * @return texto normalizado, ou {@code null} se a entrada for {@code null}
     */
    public static String normalizar(String texto) {
        if (texto == null) {
            return null;
        }
        return texto.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }
}
