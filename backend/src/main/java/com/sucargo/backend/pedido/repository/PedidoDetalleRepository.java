package com.sucargo.backend.pedido.repository;

import com.sucargo.backend.pedido.entity.PedidoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, String> {
}