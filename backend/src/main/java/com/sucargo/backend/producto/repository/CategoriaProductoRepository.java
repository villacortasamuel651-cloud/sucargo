package com.sucargo.backend.producto.repository;

import com.sucargo.backend.producto.entity.CategoriaProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, String> {

    List<CategoriaProducto> findByEmpresaId(String empresaId);

    // Para validar que una categoría pertenece a la empresa antes de asignarla a un producto
    Optional<CategoriaProducto> findByIdAndEmpresaId(String id, String empresaId);
}