package com.sucargo.backend.packing.service;

public class BultoInvalidoException extends RuntimeException {
    public BultoInvalidoException(String mensaje) {
        super(mensaje);
    }
}