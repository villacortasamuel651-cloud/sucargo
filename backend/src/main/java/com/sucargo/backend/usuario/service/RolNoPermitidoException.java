package com.sucargo.backend.usuario.service;

public class RolNoPermitidoException extends RuntimeException {
    public RolNoPermitidoException() {
        super("No se puede asignar el rol ADMIN_SUC desde este endpoint");
    }
}