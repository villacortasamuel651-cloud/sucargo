package com.sucargo.backend.distribucion.repository;

import com.sucargo.backend.distribucion.entity.DistribucionPedido;
import com.sucargo.backend.pedido.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DistribucionPedidoRepository extends JpaRepository<DistribucionPedido, String> {

    @Query("""
            SELECT COUNT(dp) > 0
            FROM DistribucionPedido dp
            WHERE dp.pedido.id = :pedidoId
            AND dp.distribucion.estado IN (
                com.sucargo.backend.distribucion.entity.Distribucion.Estado.ABIERTA,
                com.sucargo.backend.distribucion.entity.Distribucion.Estado.CONFIRMADA
            )
            """)
    boolean existsByPedidoIdInDistribucionActiva(@Param("pedidoId") String pedidoId);

    Optional<DistribucionPedido> findByDistribucionIdAndPedidoId(
            String distribucionId,
            String pedidoId
    );
}