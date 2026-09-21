package com.sucargo.backend.pedido.dto;

public record PedidoItemResponse(
        String id,
        ProductoResumenDTO producto,
        Integer cantidad,
        Integer cantidadReservada
) {}