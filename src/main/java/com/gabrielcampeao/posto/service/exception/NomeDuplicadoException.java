package com.gabrielcampeao.posto.service.exception;

public class NomeDuplicadoException extends RuntimeException {

    public NomeDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
