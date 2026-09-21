package com.sucargo.backend.cliente.service;

public class ClienteNoEncontradoException extends RuntimeException {
    public ClienteNoEncontradoException() {
        super("Cliente no encontrado");
    }
}