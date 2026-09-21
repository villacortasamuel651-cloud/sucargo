package com.sucargo.backend.picking.dto;

public record PickingLineaDTO(

        String id,

        ProductoResumenDTO producto,

        Integer cantidadEsperada,

        Integer cantidadRecolectada,

        String incidencia

) {}