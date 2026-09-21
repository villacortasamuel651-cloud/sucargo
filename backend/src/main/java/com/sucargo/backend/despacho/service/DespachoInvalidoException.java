package com.sucargo.backend.despacho.service;

public class DespachoInvalidoException extends RuntimeException {
    public DespachoInvalidoException(String mensaje) {
        super(mensaje);
    }
}