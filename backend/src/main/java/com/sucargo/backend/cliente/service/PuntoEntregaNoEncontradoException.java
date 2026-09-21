package com.sucargo.backend.cliente.service;

public class PuntoEntregaNoEncontradoException extends RuntimeException {
    public PuntoEntregaNoEncontradoException() {
        super("Punto de entrega no encontrado");
    }
}