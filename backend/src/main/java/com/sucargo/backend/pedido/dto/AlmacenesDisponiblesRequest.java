package com.sucargo.backend.pedido.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AlmacenesDisponiblesRequest(

        @NotEmpty(message = "Debe indicar al menos un producto")
        @Valid
        List<PedidoItemDTO> items

) {}