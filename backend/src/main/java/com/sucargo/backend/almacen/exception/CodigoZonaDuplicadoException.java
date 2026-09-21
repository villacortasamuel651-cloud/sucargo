package com.sucargo.backend.almacen.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CodigoZonaDuplicadoException extends RuntimeException {
    public CodigoZonaDuplicadoException(String message) {
        super(message);
    }
}