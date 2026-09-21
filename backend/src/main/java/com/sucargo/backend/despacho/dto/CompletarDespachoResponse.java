package com.sucargo.backend.despacho.dto;

public record CompletarDespachoResponse(
        String despachoId,
        String estado,
        String pedidoEstado
) {}