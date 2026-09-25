package com.sucargo.backend.empresa.service;

import com.sucargo.backend.empresa.entity.Empresa;
import com.sucargo.backend.empresa.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public List<Empresa> listarTodas() {
        return empresaRepository.findAll();
    }

    public Empresa obtenerPorId(String id) {
        return empresaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Empresa no encontrada"));
    }

    public List<Empresa> listarActivas() {
        return empresaRepository.findByEstado(Empresa.Estado.ACTIVO);
    }

    public List<Empresa> listarInactivas() {
        return empresaRepository.findByEstado(Empresa.Estado.INACTIVO);
    }
}