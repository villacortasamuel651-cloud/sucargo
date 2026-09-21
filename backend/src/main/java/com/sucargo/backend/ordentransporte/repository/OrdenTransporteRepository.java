package com.sucargo.backend.ordentransporte.repository;

import com.sucargo.backend.ordentransporte.entity.OrdenTransporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenTransporteRepository
        extends JpaRepository<OrdenTransporte, String> {

    @Query("""
            SELECT ot
            FROM OrdenTransporte ot
            JOIN FETCH ot.pedido p
            JOIN FETCH p.puntoEntrega pe
            JOIN FETCH ot.distribucion d
            JOIN FETCH d.transportista t
            WHERE ot.distribucion.id = :distribucionId
            AND d.empresaId = :empresaId
            """)
    List<OrdenTransporte> findByDistribucionIdAndEmpresaId(
            @Param("distribucionId") String distribucionId,
            @Param("empresaId") String empresaId
    );

    @Query("""
            SELECT ot
            FROM OrdenTransporte ot
            JOIN FETCH ot.pedido p
            JOIN FETCH p.puntoEntrega pe
            JOIN FETCH ot.distribucion d
            JOIN FETCH d.transportista t
            WHERE ot.distribucion.id = :distribucionId
            AND d.empresaId = :empresaId
            AND ot.estado = :estado
            """)
    List<OrdenTransporte> findByDistribucionIdAndEstadoAndEmpresaId(
            @Param("distribucionId") String distribucionId,
            @Param("estado") String estado,
            @Param("empresaId") String empresaId
    );

    @Query("""
            SELECT ot
            FROM OrdenTransporte ot
            JOIN FETCH ot.pedido p
            JOIN FETCH p.puntoEntrega pe
            JOIN FETCH ot.distribucion d
            JOIN FETCH d.transportista t
            WHERE ot.estado = :estado
            AND d.empresaId = :empresaId
            """)
    List<OrdenTransporte> findByEstadoAndEmpresaId(
            @Param("estado") String estado,
            @Param("empresaId") String empresaId
    );

    @Query("""
            SELECT ot
            FROM OrdenTransporte ot
            JOIN FETCH ot.pedido p
            JOIN FETCH p.puntoEntrega pe
            JOIN FETCH ot.distribucion d
            JOIN FETCH d.transportista t
            WHERE d.empresaId = :empresaId
            """)
    List<OrdenTransporte> findAllByEmpresaId(
            @Param("empresaId") String empresaId
    );

    @Query("""
            SELECT ot
            FROM OrdenTransporte ot
            JOIN FETCH ot.pedido p
            JOIN FETCH p.puntoEntrega pe
            JOIN FETCH ot.distribucion d
            JOIN FETCH d.transportista t
            WHERE ot.id = :id
            AND d.empresaId = :empresaId
            """)
    Optional<OrdenTransporte> findByIdWithDetailsAndEmpresaId(
            @Param("id") String id,
            @Param("empresaId") String empresaId
    );

    @Query("""
            SELECT COUNT(ot) > 0
            FROM OrdenTransporte ot
            JOIN ot.distribucion d
            WHERE d.id = :distribucionId
            AND d.empresaId = :empresaId
            """)
    boolean existsByDistribucionIdAndEmpresaId(
            @Param("distribucionId") String distribucionId,
            @Param("empresaId") String empresaId
    );

    @Query("""
            SELECT COUNT(ot) > 0
            FROM OrdenTransporte ot
            JOIN ot.distribucion d
            WHERE ot.pedido.id = :pedidoId
            AND d.empresaId = :empresaId
            """)
    boolean existsByPedidoIdAndEmpresaId(
            @Param("pedidoId") String pedidoId,
            @Param("empresaId") String empresaId
    );


    @Query("""
        SELECT ot
        FROM OrdenTransporte ot
        JOIN FETCH ot.pedido p
        JOIN FETCH p.puntoEntrega pe
        JOIN FETCH ot.distribucion d
        JOIN FETCH d.transportista t
        WHERE ot.pedido.id = :pedidoId
        AND d.empresaId = :empresaId
        """)
Optional<OrdenTransporte> findByPedidoIdAndEmpresaId(
        @Param("pedidoId") String pedidoId,
        @Param("empresaId") String empresaId
);








}