package com.sucargo.backend.producto.service;

import com.sucargo.backend.producto.dto.CategoriaProductoResponse;
import com.sucargo.backend.producto.dto.ProductoRequest;
import com.sucargo.backend.producto.dto.ProductoResponse;
import com.sucargo.backend.producto.entity.CategoriaProducto;
import com.sucargo.backend.producto.entity.Producto;
import com.sucargo.backend.producto.repository.CategoriaProductoRepository;
import com.sucargo.backend.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaProductoRepository categoriaProductoRepository;

    public List<ProductoResponse> listar(
            String empresaId, String estadoFiltro, String textoFiltro, String categoriaIdFiltro) {

        List<Producto> productos;

        if (textoFiltro != null && !textoFiltro.isBlank()) {
            productos = productoRepository.buscarPorTexto(empresaId, textoFiltro);
        } else if (estadoFiltro != null && !estadoFiltro.isBlank()) {
            productos = productoRepository.findByEmpresaIdAndEstado(
                    empresaId, Producto.Estado.valueOf(estadoFiltro));
        } else {
            productos = productoRepository.findByEmpresaIdAndEstado(empresaId, Producto.Estado.ACTIVO);
        }

        // Filtro adicional por categoría, si viene en la query
        if (categoriaIdFiltro != null && !categoriaIdFiltro.isBlank()) {
            productos = productos.stream()
                    .filter(p -> p.getCategoria() != null
                            && categoriaIdFiltro.equals(p.getCategoria().getId()))
                    .toList();
        }

        return productos.stream().map(this::toDTO).toList();
    }

    public ProductoResponse obtener(String id, String empresaId) {
        Producto producto = buscarProductoDeEmpresa(id, empresaId);
        return toDTO(producto);
    }

    @SuppressWarnings("null")
    @Transactional
    public ProductoResponse crear(ProductoRequest request, String empresaId) {
        if (productoRepository.existsByEmpresaIdAndSku(empresaId, request.sku())) {
            throw new SkuYaExisteException();
        }

        CategoriaProducto categoria = resolverCategoria(request.categoriaId(), empresaId);

        Producto producto = Producto.builder()
                .empresaId(empresaId)
                .categoria(categoria)
                .sku(request.sku())
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .unidadMedida(request.unidadMedida())
                .peso(request.peso())
                .stockMinimo(request.stockMinimo() != null ? request.stockMinimo() : 0)
                .estado(Producto.Estado.ACTIVO)
                .build();

        Producto guardado = productoRepository.save(producto);
        return toDTO(guardado);
    }

    @Transactional
    public ProductoResponse editar(String id, ProductoRequest request, String empresaId) {
        Producto producto = buscarProductoDeEmpresa(id, empresaId);

        // Si cambió el SKU, hay que validar que el nuevo no choque con otro producto
        if (!producto.getSku().equals(request.sku())
                && productoRepository.existsByEmpresaIdAndSku(empresaId, request.sku())) {
            throw new SkuYaExisteException();
        }

        CategoriaProducto categoria = resolverCategoria(request.categoriaId(), empresaId);

        producto.setSku(request.sku());
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setCategoria(categoria);
        producto.setUnidadMedida(request.unidadMedida());
        producto.setPeso(request.peso());
        producto.setStockMinimo(request.stockMinimo() != null ? request.stockMinimo() : 0);

        Producto actualizado = productoRepository.save(producto);
        return toDTO(actualizado);
    }

    @Transactional
    public void desactivar(String id, String empresaId) {
        Producto producto = buscarProductoDeEmpresa(id, empresaId);
        producto.setEstado(Producto.Estado.INACTIVO);
        productoRepository.save(producto);
    }

    // --- Helpers privados ---

    private Producto buscarProductoDeEmpresa(String id, String empresaId) {
        return productoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(ProductoNoEncontradoException::new);
    }

    // Si viene categoriaId, la busca y valida que sea de la misma empresa.
    // Si no viene (null), el producto queda sin categoría (es opcional).
    private CategoriaProducto resolverCategoria(String categoriaId, String empresaId) {
        if (categoriaId == null || categoriaId.isBlank()) {
            return null;
        }
        return categoriaProductoRepository.findByIdAndEmpresaId(categoriaId, empresaId)
                .orElseThrow(CategoriaNoEncontradaException::new);
    }

    private ProductoResponse toDTO(Producto producto) {
        CategoriaProductoResponse categoriaDTO = producto.getCategoria() != null
                ? new CategoriaProductoResponse(
                        producto.getCategoria().getId(), producto.getCategoria().getNombre())
                : null;

        return new ProductoResponse(
                producto.getId(),
                producto.getSku(),
                producto.getNombre(),
                producto.getDescripcion(),
                categoriaDTO,
                producto.getUnidadMedida(),
                producto.getPeso(),
                producto.getStockMinimo(),
                producto.getEstado().name()
        );
    }
}