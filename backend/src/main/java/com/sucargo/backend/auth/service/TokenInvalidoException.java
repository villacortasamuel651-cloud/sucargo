package com.sucargo.backend.auth.service;

public class TokenInvalidoException extends RuntimeException {
    public TokenInvalidoException() {
        super("Token inválido o expirado");
    }
}