
package com.sucargo.backend.ordentransporte.dto;

import java.math.BigDecimal;

public record OrdenTransporteDetailResponse(
        String id,
        String numero,
        PedidoResumenDTO pedido,
        AlmacenResumenDTO origenAlmacen,
        DestinoDTO destino,
        String transportista,
        Integer totalBultos,
        BigDecimal pesoTotal,
        String ventanaInicio,
        String ventanaFin,
        String estado
) {

    public record PedidoResumenDTO(
            String id,
            String codigo
    ) {}

    public record AlmacenResumenDTO(
            String id,
            String nombre
    ) {}

    public record DestinoDTO(
            String direccion,
            String distrito
    ) {}
}

