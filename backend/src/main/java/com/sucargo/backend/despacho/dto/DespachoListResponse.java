package com.sucargo.backend.despacho.dto;

public record DespachoListResponse(
        String id,
        PedidoResumenDTO pedido,
        String estado,
        int bultosVerificados,
        int totalBultos
) {}