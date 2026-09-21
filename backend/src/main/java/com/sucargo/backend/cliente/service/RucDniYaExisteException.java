package com.sucargo.backend.cliente.service;

public class RucDniYaExisteException extends RuntimeException {
    public RucDniYaExisteException() {
        super("El RUC/DNI ya está registrado en esta empresa");
    }
}