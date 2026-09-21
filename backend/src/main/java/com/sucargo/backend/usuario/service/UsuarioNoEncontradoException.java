package com.sucargo.backend.usuario.service;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException() {
        super("Usuario no encontrado");
    }
}