package com.sucargo.backend.pedido.service;

public class PedidoNoCancelableException extends RuntimeException {
    public PedidoNoCancelableException() {
        super("El pedido no puede cancelarse en su estado actual");
    }
}