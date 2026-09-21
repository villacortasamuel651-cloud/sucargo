package com.sucargo.backend.cliente.repository;

import com.sucargo.backend.cliente.entity.PuntoEntrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PuntoEntregaRepository extends JpaRepository<PuntoEntrega, String> {

    List<PuntoEntrega> findByClienteId(String clienteId);

    // Trae un punto de entrega, pero SOLO si pertenece a ese cliente
    // (evita que alguien edite/borre un punto de entrega de otro cliente)
    Optional<PuntoEntrega> findByIdAndClienteId(String id, String clienteId);

    long countByClienteIdAndEstado(String clienteId, PuntoEntrega.Estado estado);
}