package com.sucargo.backend.transportista.repository;

import com.sucargo.backend.transportista.entity.Transportista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransportistaRepository extends JpaRepository<Transportista, String> {

    @Query("SELECT t FROM Transportista t WHERE t.empresaId = :empresaId AND (:estado IS NULL OR t.estado = :estado)")
    List<Transportista> buscar(@Param("empresaId") String empresaId, @Param("estado") Transportista.Estado estado);

    Optional<Transportista> findByIdAndEmpresaId(String id, String empresaId);
}