package com.sucargo.backend.despacho.service;

public class DespachoNoEncontradoException extends RuntimeException {
    public DespachoNoEncontradoException() {
        super("Despacho no encontrado");
    }
}