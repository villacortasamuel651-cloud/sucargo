package com.sucargo.backend.inventario.service;

public class RolNoAutorizadoException extends RuntimeException {
    public RolNoAutorizadoException() {
        super("Tu rol no está autorizado para registrar movimientos de inventario");
    }
}