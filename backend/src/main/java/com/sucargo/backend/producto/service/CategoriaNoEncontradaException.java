package com.sucargo.backend.producto.service;

public class CategoriaNoEncontradaException extends RuntimeException {
    public CategoriaNoEncontradaException() {
        super("Categoría no encontrada");
    }
}