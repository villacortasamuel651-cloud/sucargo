package com.sucargo.backend.producto.service;

import com.sucargo.backend.producto.dto.CategoriaProductoRequest;
import com.sucargo.backend.producto.dto.CategoriaProductoResponse;
import com.sucargo.backend.producto.entity.CategoriaProducto;
import com.sucargo.backend.producto.repository.CategoriaProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaProductoService {

    private final CategoriaProductoRepository categoriaProductoRepository;

    public List<CategoriaProductoResponse> listar(String empresaId) {
        return categoriaProductoRepository.findByEmpresaId(empresaId).stream()
                .map(c -> new CategoriaProductoResponse(c.getId(), c.getNombre()))
                .toList();
    }

    @SuppressWarnings("null")
    public CategoriaProductoResponse crear(CategoriaProductoRequest request, String empresaId) {
        CategoriaProducto categoria = CategoriaProducto.builder()
                .empresaId(empresaId)
                .nombre(request.nombre())
                .build();

        CategoriaProducto guardada = categoriaProductoRepository.save(categoria);
        return new CategoriaProductoResponse(guardada.getId(), guardada.getNombre());
    }
}