package com.sucargo.backend.inventario.repository;

import com.sucargo.backend.inventario.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, String> {

    List<MovimientoInventario> findByInventarioIdOrderByCreatedAtDesc(String inventarioId);
}