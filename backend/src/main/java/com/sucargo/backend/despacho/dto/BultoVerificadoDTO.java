package com.sucargo.backend.despacho.dto;

public record BultoVerificadoDTO(
        String id,
        String codigo,
        Integer numero,
        Boolean verificado
) {}