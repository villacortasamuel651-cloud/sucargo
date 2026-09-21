package com.sucargo.backend.cliente.repository;

import com.sucargo.backend.cliente.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, String> {

    List<Cliente> findByEmpresaIdAndEstado(String empresaId, Cliente.Estado estado);

    // Trae un cliente por id, pero SOLO si pertenece a esa empresa
    // (así evitamos que alguien acceda a datos de otra empresa por id directo)
    Optional<Cliente> findByIdAndEmpresaId(String id, String empresaId);

    boolean existsByEmpresaIdAndRucDni(String empresaId, String rucDni);

    // Búsqueda por texto: nombre (razón social) o RUC/DNI, dentro de la empresa
    @Query("""
            SELECT c FROM Cliente c
            WHERE c.empresaId = :empresaId
            AND (LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :texto, '%'))
                 OR c.rucDni LIKE CONCAT('%', :texto, '%'))
            """)
    List<Cliente> buscarPorTexto(@Param("empresaId") String empresaId, @Param("texto") String texto);
}