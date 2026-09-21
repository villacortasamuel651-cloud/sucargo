package com.sucargo.backend.picking.repository;

import com.sucargo.backend.picking.entity.Picking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PickingRepository extends JpaRepository<Picking, String> {

    @Query("""
            SELECT pk
            FROM Picking pk
            JOIN pk.pedido p
            WHERE p.empresaId = :empresaId
            AND (:estado IS NULL OR pk.estado = :estado)
            """)
    List<Picking> buscar(
            @Param("empresaId") String empresaId,
            @Param("estado") Picking.Estado estado
    );

    @Query("""
            SELECT pk
            FROM Picking pk
            JOIN pk.pedido p
            WHERE pk.id = :id
            AND p.empresaId = :empresaId
            """)
    Optional<Picking> findByIdAndEmpresaId(
            @Param("id") String id,
            @Param("empresaId") String empresaId
    );

    @Query("""
            SELECT COUNT(pk) > 0
            FROM Picking pk
            JOIN pk.pedido p
            WHERE pk.pedido.id = :pedidoId
            AND p.empresaId = :empresaId
            """)
    boolean existsByPedidoIdAndEmpresaId(
            @Param("pedidoId") String pedidoId,
            @Param("empresaId") String empresaId
    );

    @Query("""
            SELECT pk.id
            FROM Picking pk
            JOIN pk.pedido p
            WHERE pk.pedido.id = :pedidoId
            AND p.empresaId = :empresaId
            """)
    Optional<String> findPickingIdByPedidoIdAndEmpresaId(
            @Param("pedidoId") String pedidoId,
            @Param("empresaId") String empresaId
    );
}