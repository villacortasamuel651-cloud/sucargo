package com.sucargo.backend.packing.service;

public class BultoNoEncontradoException extends RuntimeException {
    public BultoNoEncontradoException() {
        super("Bulto no encontrado");
    }
}