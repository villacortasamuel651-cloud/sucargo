package com.sucargo.backend.almacen.repository;

import com.sucargo.backend.almacen.entity.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlmacenRepository extends JpaRepository<Almacen, String> {

    List<Almacen> findByEmpresaIdAndEstado(String empresaId, Almacen.Estado estado);

    // Trae un almacén por id, pero SOLO si pertenece a esa empresa
    Optional<Almacen> findByIdAndEmpresaId(String id, String empresaId);
}