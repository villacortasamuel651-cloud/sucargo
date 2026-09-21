package com.sucargo.backend.despacho.dto;

import java.util.List;

public record DespachoDetailResponse(
        String id,
        PedidoResumenDTO pedido,
        String ordenTransporte,
        String estado,
        List<BultoVerificadoDTO> bultos
) {}