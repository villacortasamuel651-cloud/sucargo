package com.sucargo.backend.pedido.dto;

import com.sucargo.backend.almacen.dto.AlmacenResumenDTO;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoDetailResponse(

        String id,

        String codigo,

        ClienteResumenDTO cliente,

        PuntoEntregaResumenDTO puntoEntrega,

        AlmacenResumenDTO almacen,

        String estado,

        String prioridad,

        String usuario,

        LocalDateTime fecha,

        List<PedidoItemResponse> items

) {
}