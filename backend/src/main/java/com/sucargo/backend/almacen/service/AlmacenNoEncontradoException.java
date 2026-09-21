package com.sucargo.backend.almacen.service;

public class AlmacenNoEncontradoException extends RuntimeException {

    public AlmacenNoEncontradoException() {
        super("Almacén no encontrado");
    }
}