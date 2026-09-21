package com.sucargo.backend.distribucion.service;

public class DistribucionInvalidaException extends RuntimeException {
    public DistribucionInvalidaException(String mensaje) {
        super(mensaje);
    }
}