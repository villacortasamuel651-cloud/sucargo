package com.sucargo.backend.picking.repository;

import com.sucargo.backend.picking.entity.PickingDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickingDetalleRepository extends JpaRepository<PickingDetalle, String> {
}