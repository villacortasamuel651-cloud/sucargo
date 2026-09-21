package com.sucargo.backend.almacen.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlmacenResponse(
        String id,
        String nombre,
        String direccion,
        String estado
) {

    public static AlmacenResponse paraLista(
            String id,
            String nombre,
            String direccion,
            String estado) {

        return new AlmacenResponse(
                id,
                nombre,
                direccion,
                estado
        );
    }

    public static AlmacenResponse paraDetalle(
            String id,
            String nombre,
            String direccion,
            String estado) {

        return new AlmacenResponse(
                id,
                nombre,
                direccion,
                estado
        );
    }
}