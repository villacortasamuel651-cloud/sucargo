package com.sucargo.backend.pedido.dto;

import com.sucargo.backend.almacen.dto.AlmacenResumenDTO;

import java.time.LocalDateTime;

public record PedidoListResponse(

        String id,

        String codigo,

        ClienteResumenDTO cliente,

        AlmacenResumenDTO almacen,

        String estado,

        String prioridad,

        int totalItems,

        LocalDateTime fecha

) {
}