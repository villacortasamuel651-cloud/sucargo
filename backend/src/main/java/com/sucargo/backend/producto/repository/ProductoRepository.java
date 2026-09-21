package com.sucargo.backend.producto.repository;

import com.sucargo.backend.producto.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, String> {

    List<Producto> findByEmpresaIdAndEstado(String empresaId, Producto.Estado estado);

    // Trae un producto por id, pero SOLO si pertenece a esa empresa
    Optional<Producto> findByIdAndEmpresaId(String id, String empresaId);

    boolean existsByEmpresaIdAndSku(String empresaId, String sku);

    // Búsqueda por texto: nombre o SKU, dentro de la empresa
    @Query("""
            SELECT p FROM Producto p
            WHERE p.empresaId = :empresaId
            AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                 OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :texto, '%')))
            """)
    List<Producto> buscarPorTexto(@Param("empresaId") String empresaId, @Param("texto") String texto);
}