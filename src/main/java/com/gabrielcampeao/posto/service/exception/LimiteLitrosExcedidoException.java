package com.gabrielcampeao.posto.service.exception;

public class LimiteLitrosExcedidoException extends RuntimeException {

    public LimiteLitrosExcedidoException(String mensagem) {
        super(mensagem);
    }
}
