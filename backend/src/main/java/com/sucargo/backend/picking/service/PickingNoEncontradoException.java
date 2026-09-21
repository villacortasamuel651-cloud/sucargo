package com.sucargo.backend.picking.service;

public class PickingNoEncontradoException extends RuntimeException {
    public PickingNoEncontradoException() {
        super("Picking no encontrado");
    }
}