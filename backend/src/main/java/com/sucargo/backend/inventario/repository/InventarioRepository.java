package com.sucargo.backend.inventario.repository;

import com.sucargo.backend.inventario.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, String> {

    @Query("""
            SELECT i FROM Inventario i
            JOIN i.producto p
            JOIN i.almacen a
            WHERE p.empresaId = :empresaId
            AND (:almacenId IS NULL OR a.id = :almacenId)
            AND (:productoId IS NULL OR p.id = :productoId)
            AND (:texto IS NULL
                 OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                 OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :texto, '%')))
            """)
    List<Inventario> listar(
            @Param("empresaId") String empresaId,
            @Param("almacenId") String almacenId,
            @Param("productoId") String productoId,
            @Param("texto") String texto);

    @Query("""
            SELECT i FROM Inventario i
            JOIN i.producto p
            WHERE i.id = :id
            AND p.empresaId = :empresaId
            """)
    Optional<Inventario> findByIdAndEmpresaId(
            @Param("id") String id,
            @Param("empresaId") String empresaId);

    @Query("""
            SELECT i.producto.id, SUM(i.stockFisico)
            FROM Inventario i
            JOIN i.producto p
            WHERE p.empresaId = :empresaId
            GROUP BY i.producto.id
            """)
    List<Object[]> sumarStockPorProducto(
            @Param("empresaId") String empresaId);
}