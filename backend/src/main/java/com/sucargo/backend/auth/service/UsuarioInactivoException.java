package com.sucargo.backend.auth.service;

public class UsuarioInactivoException extends RuntimeException {
    public UsuarioInactivoException() {
        super("Usuario inactivo");
    }
}