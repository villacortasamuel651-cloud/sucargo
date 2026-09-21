package com.sucargo.backend.inventario.dto;

public record InventarioResponse(

        String id,

        ProductoResumenDTO producto,

        AlmacenResumenDTO almacen,

        Integer stockFisico,

        Integer stockReservado,

        Integer stockDisponible,

        String estado

) {
}