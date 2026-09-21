package com.sucargo.backend.distribucion.repository;

import com.sucargo.backend.distribucion.entity.Distribucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DistribucionRepository extends JpaRepository<Distribucion, String> {

    @Query("SELECT d FROM Distribucion d WHERE d.empresaId = :empresaId " +
           "AND (:estado IS NULL OR d.estado = :estado) " +
           "AND (:fecha IS NULL OR d.fecha = :fecha)")
    List<Distribucion> buscar(
            @Param("empresaId") String empresaId,
            @Param("estado") Distribucion.Estado estado,
            @Param("fecha") LocalDate fecha
    );

    Optional<Distribucion> findByIdAndEmpresaId(String id, String empresaId);
}