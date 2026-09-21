package com.sucargo.backend.pedido.service;

public class PedidoInvalidoException extends RuntimeException {
    public PedidoInvalidoException(String mensaje) {
        super(mensaje);
    }
}