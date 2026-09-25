package com.sucargo.backend.empresa.repository;

import com.sucargo.backend.empresa.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, String> {

    Optional<Empresa> findByRuc(String ruc);

    boolean existsByRuc(String ruc);

    List<Empresa> findByEstado(Empresa.Estado estado);
}