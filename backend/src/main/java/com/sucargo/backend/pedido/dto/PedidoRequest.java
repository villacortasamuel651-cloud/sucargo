package com.sucargo.backend.pedido.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PedidoRequest(

        @NotBlank
        String clienteId,

        @NotBlank
        String puntoEntregaId,

        @NotBlank
        String almacenId,

        String prioridad,

        @NotEmpty(message = "El pedido debe tener al menos un item")
        @Valid
        List<PedidoItemDTO> items

) {}