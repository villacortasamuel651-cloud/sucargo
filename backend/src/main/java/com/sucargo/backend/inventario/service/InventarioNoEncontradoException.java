package com.sucargo.backend.inventario.service;

public class InventarioNoEncontradoException extends RuntimeException {
    public InventarioNoEncontradoException() {
        super("Inventario no encontrado");
    }
}