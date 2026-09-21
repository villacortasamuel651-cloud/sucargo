package com.sucargo.backend.picking.service;

public class PickingInvalidoException extends RuntimeException {
    public PickingInvalidoException(String message) {
        super(message);
    }
}