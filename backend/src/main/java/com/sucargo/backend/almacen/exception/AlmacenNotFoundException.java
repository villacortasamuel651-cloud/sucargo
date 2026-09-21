package com.sucargo.backend.almacen.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AlmacenNotFoundException extends RuntimeException {
    public AlmacenNotFoundException(String message) {
        super(message);
    }
}