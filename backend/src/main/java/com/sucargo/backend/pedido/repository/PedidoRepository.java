package com.sucargo.backend.pedido.repository;

import com.sucargo.backend.pedido.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, String> {

    @Query("""
            SELECT p FROM Pedido p
            JOIN p.cliente c
            WHERE p.empresaId = :empresaId
            AND (:estado IS NULL OR p.estado = :estado)
            AND (:clienteId IS NULL OR c.id = :clienteId)
            AND (:texto IS NULL
                 OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :texto, '%'))
                 OR LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :texto, '%')))
            ORDER BY p.createdAt DESC
            """)
    List<Pedido> buscar(
            @Param("empresaId") String empresaId,
            @Param("estado") Pedido.Estado estado,
            @Param("clienteId") String clienteId,
            @Param("texto") String texto);

    Optional<Pedido> findByIdAndEmpresaId(String id, String empresaId);

    @Query("""
            SELECT p FROM Pedido p
            JOIN p.cliente c
            WHERE p.empresaId = :empresaId
            AND p.estado = com.sucargo.backend.pedido.entity.Pedido.Estado.PACKING_COMPLETADO
            AND NOT EXISTS (
                SELECT dp.id
                FROM DistribucionPedido dp
                WHERE dp.pedido.id = p.id
                AND dp.distribucion.estado IN (
                    com.sucargo.backend.distribucion.entity.Distribucion.Estado.ABIERTA,
                    com.sucargo.backend.distribucion.entity.Distribucion.Estado.CONFIRMADA
                )
            )
            ORDER BY p.createdAt DESC
            """)
    List<Pedido> buscarDisponiblesParaDistribucion(
            @Param("empresaId") String empresaId);
}