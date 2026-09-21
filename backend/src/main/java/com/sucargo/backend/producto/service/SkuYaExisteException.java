package com.sucargo.backend.producto.service;

public class SkuYaExisteException extends RuntimeException {
    public SkuYaExisteException() {
        super("El SKU ya está registrado en esta empresa");
    }
}