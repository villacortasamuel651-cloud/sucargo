package com.sucargo.backend.producto.service;

public class ProductoNoEncontradoException extends RuntimeException {
    public ProductoNoEncontradoException() {
        super("Producto no encontrado");
    }
}