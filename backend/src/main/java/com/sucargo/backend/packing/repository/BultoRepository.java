package com.sucargo.backend.packing.repository;

import com.sucargo.backend.packing.entity.Bulto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BultoRepository extends JpaRepository<Bulto, String> {

    @Query("""
            SELECT b
            FROM Bulto b
            JOIN b.pedido p
            WHERE p.id = :pedidoId
            AND p.empresaId = :empresaId
            ORDER BY b.numero ASC
            """)
    List<Bulto> findByPedidoIdAndEmpresaIdOrderByNumeroAsc(
            @Param("pedidoId") String pedidoId,
            @Param("empresaId") String empresaId
    );

    @Query("""
            SELECT COUNT(b)
            FROM Bulto b
            JOIN b.pedido p
            WHERE p.id = :pedidoId
            AND p.empresaId = :empresaId
            """)
    long countByPedidoIdAndEmpresaId(
            @Param("pedidoId") String pedidoId,
            @Param("empresaId") String empresaId
    );

    @Query("""
            SELECT b
            FROM Bulto b
            JOIN b.pedido p
            WHERE b.id = :id
            AND p.empresaId = :empresaId
            """)
    Optional<Bulto> findByIdAndEmpresaId(
            @Param("id") String id,
            @Param("empresaId") String empresaId
    );
}
