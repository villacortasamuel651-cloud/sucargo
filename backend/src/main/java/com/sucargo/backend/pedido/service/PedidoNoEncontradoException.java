package com.sucargo.backend.pedido.service;

public class PedidoNoEncontradoException extends RuntimeException {
    public PedidoNoEncontradoException() {
        super("Pedido no encontrado");
    }
}