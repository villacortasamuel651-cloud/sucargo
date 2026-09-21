package com.sucargo.backend.auth.repository;

import com.sucargo.backend.auth.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // NUEVO: para listar todos los usuarios de una empresa (paso 1 del contrato)
    List<Usuario> findByEmpresaId(String empresaId);

    // NUEVO: para buscar un usuario por id, pero SOLO si es de esa empresa
    // (mismo patrón de seguridad que usamos en ClienteRepository)
    Optional<Usuario> findByIdAndEmpresaId(String id, String empresaId);
}