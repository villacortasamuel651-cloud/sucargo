package com.sucargo.backend.despacho.repository;

import com.sucargo.backend.despacho.entity.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DespachoRepository extends JpaRepository<Despacho, String> {

    @Query("""
            SELECT d
            FROM Despacho d
            JOIN d.pedido p
            WHERE p.empresaId = :empresaId
            AND (:estado IS NULL OR d.estado = :estado)
            ORDER BY d.id DESC
            """)
    List<Despacho> buscar(
            @Param("empresaId") String empresaId,
            @Param("estado") Despacho.Estado estado
    );

    @Query("""
            SELECT d
            FROM Despacho d
            JOIN d.pedido p
            WHERE d.id = :id
            AND p.empresaId = :empresaId
            """)
    Optional<Despacho> findByIdAndEmpresaId(
            @Param("id") String id,
            @Param("empresaId") String empresaId
    );

    @Query("""
            SELECT COUNT(d) > 0
            FROM Despacho d
            JOIN d.pedido p
            WHERE d.pedido.id = :pedidoId
            AND p.empresaId = :empresaId
            """)
    boolean existsByPedidoIdAndEmpresaId(
            @Param("pedidoId") String pedidoId,
            @Param("empresaId") String empresaId
    );
}