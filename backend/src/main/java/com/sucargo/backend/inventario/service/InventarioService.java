package com.sucargo.backend.inventario.service;

import com.sucargo.backend.almacen.entity.Almacen;
import com.sucargo.backend.almacen.repository.AlmacenRepository;
import com.sucargo.backend.inventario.dto.*;
import com.sucargo.backend.inventario.entity.Inventario;
import com.sucargo.backend.inventario.entity.MovimientoInventario;
import com.sucargo.backend.inventario.repository.InventarioRepository;
import com.sucargo.backend.inventario.repository.MovimientoInventarioRepository;
import com.sucargo.backend.producto.repository.ProductoRepository;
import com.sucargo.backend.producto.service.ProductoNoEncontradoException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sucargo.backend.almacen.service.AlmacenNoEncontradoException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private static final Set<String> ROLES_PERMITIDOS_MOVIMIENTO =
            Set.of("ADMIN_EMPRESA", "ALMACENERO");

    private final InventarioRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoRepository productoRepository;
    private final AlmacenRepository almacenRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<InventarioResponse> listar(
            String empresaId,
            String almacenId,
            String productoId,
            String texto,
            Boolean stockBajo) {

        List<Inventario> inventarios = inventarioRepository.listar(
                empresaId,
                almacenId,
                productoId,
                (texto != null && !texto.isBlank()) ? texto : null
        );

        // ProductoId -> stock físico total de todos sus almacenes
        Map<String, Long> stockTotalPorProducto = new HashMap<>();

        for (Object[] fila : inventarioRepository.sumarStockPorProducto(empresaId)) {
            stockTotalPorProducto.put(
                    (String) fila[0],
                    (Long) fila[1]
            );
        }

        List<InventarioResponse> resultado = inventarios.stream()
                .map(inv -> toDTO(inv, stockTotalPorProducto))
                .toList();

        if (stockBajo != null && stockBajo) {
            resultado = resultado.stream()
                    .filter(r ->
                            "BAJO_STOCK".equals(r.estado())
                                    || "SIN_STOCK".equals(r.estado())
                    )
                    .toList();
        }

        return resultado;
    }

    public List<MovimientoResponse> historial(
            String inventarioId,
            String empresaId) {

        Inventario inventario = inventarioRepository
                .findByIdAndEmpresaId(inventarioId, empresaId)
                .orElseThrow(InventarioNoEncontradoException::new);

        return movimientoInventarioRepository
                .findByInventarioIdOrderByCreatedAtDesc(inventario.getId())
                .stream()
                .map(this::toMovimientoDTO)
                .toList();
    }

    @Transactional
    public ResultadoMovimientoResponse registrarMovimiento(
            MovimientoRequest request,
            String usuarioId,
            String rolUsuario,
            String empresaId) {

        if (!ROLES_PERMITIDOS_MOVIMIENTO.contains(rolUsuario)) {
            throw new RolNoAutorizadoException();
        }

        // Validamos que el producto pertenezca a la empresa
        productoRepository.findByIdAndEmpresaId(
                        request.productoId(),
                        empresaId
                )
                .orElseThrow(ProductoNoEncontradoException::new);

        // Validamos que el almacén pertenezca a la empresa
        Almacen almacen = almacenRepository
                .findByIdAndEmpresaId(
                        request.almacenId(),
                        empresaId
                )
                .orElseThrow(AlmacenNoEncontradoException::new);

        try {
            StoredProcedureQuery query = entityManager
                    .createStoredProcedureQuery(
                            "sp_registrar_movimiento_inventario"
                    )
                    .registerStoredProcedureParameter(
                            "p_producto_id",
                            String.class,
                            ParameterMode.IN
                    )
                    .registerStoredProcedureParameter(
                            "p_almacen_id",
                            String.class,
                            ParameterMode.IN
                    )
                    .registerStoredProcedureParameter(
                            "p_tipo",
                            String.class,
                            ParameterMode.IN
                    )
                    .registerStoredProcedureParameter(
                            "p_cantidad",
                            Integer.class,
                            ParameterMode.IN
                    )
                    .registerStoredProcedureParameter(
                            "p_usuario_id",
                            String.class,
                            ParameterMode.IN
                    )
                    .registerStoredProcedureParameter(
                            "p_motivo",
                            String.class,
                            ParameterMode.IN
                    )
                    .registerStoredProcedureParameter(
                            "p_inventario_id",
                            String.class,
                            ParameterMode.OUT
                    )
                    .registerStoredProcedureParameter(
                            "p_stock_fisico",
                            Integer.class,
                            ParameterMode.OUT
                    )
                    .registerStoredProcedureParameter(
                            "p_stock_reservado",
                            Integer.class,
                            ParameterMode.OUT
                    );

            query.setParameter(
                    "p_producto_id",
                    request.productoId()
            );

            query.setParameter(
                    "p_almacen_id",
                    almacen.getId()
            );

            query.setParameter(
                    "p_tipo",
                    request.tipo()
            );

            query.setParameter(
                    "p_cantidad",
                    request.cantidad()
            );

            query.setParameter(
                    "p_usuario_id",
                    usuarioId
            );

            query.setParameter(
                    "p_motivo",
                    request.motivo()
            );

            query.execute();

            String inventarioId =
                    (String) query.getOutputParameterValue(
                            "p_inventario_id"
                    );

            Integer stockFisico =
                    (Integer) query.getOutputParameterValue(
                            "p_stock_fisico"
                    );

            Integer stockReservado =
                    (Integer) query.getOutputParameterValue(
                            "p_stock_reservado"
                    );

            int stockDisponible =
                    stockFisico - stockReservado;

            return new ResultadoMovimientoResponse(
                    inventarioId,
                    stockFisico,
                    stockReservado,
                    stockDisponible
            );

        } catch (PersistenceException ex) {
            throw new MovimientoInvalidoException(
                    extraerMensajeDelSP(ex)
            );
        }
    }

    // Uso interno desde otros services, por ejemplo PedidoService.
    @Transactional
    public ResultadoMovimientoResponse registrarMovimientoInterno(
            String productoId,
            String almacenId,
            String tipo,
            int cantidad,
            String usuarioId,
            String motivo) {

        try {
            StoredProcedureQuery query = entityManager
                    .createStoredProcedureQuery(
                            "sp_registrar_movimiento_inventario"
                    )
                    .registerStoredProcedureParameter(
                            "p_producto_id",
                            String.class,
                            ParameterMode.IN
                    )
                    .registerStoredProcedureParameter(
                            "p_almacen_id",
                            String.class,
                            ParameterMode.IN
                    )
                    .registerStoredProcedureParameter(
                            "p_tipo",
                            String.class,
                            ParameterMode.IN
                    )
                    .registerStoredProcedureParameter(
                            "p_cantidad",
                            Integer.class,
                            ParameterMode.IN
                    )
                    .registerStoredProcedureParameter(
                            "p_usuario_id",
                            String.class,
                            ParameterMode.IN
                    )
                    .registerStoredProcedureParameter(
                            "p_motivo",
                            String.class,
                            ParameterMode.IN
                    )
                    .registerStoredProcedureParameter(
                            "p_inventario_id",
                            String.class,
                            ParameterMode.OUT
                    )
                    .registerStoredProcedureParameter(
                            "p_stock_fisico",
                            Integer.class,
                            ParameterMode.OUT
                    )
                    .registerStoredProcedureParameter(
                            "p_stock_reservado",
                            Integer.class,
                            ParameterMode.OUT
                    );

            query.setParameter(
                    "p_producto_id",
                    productoId
            );

            query.setParameter(
                    "p_almacen_id",
                    almacenId
            );

            query.setParameter(
                    "p_tipo",
                    tipo
            );

            query.setParameter(
                    "p_cantidad",
                    cantidad
            );

            query.setParameter(
                    "p_usuario_id",
                    usuarioId
            );

            query.setParameter(
                    "p_motivo",
                    motivo
            );

            query.execute();

            String inventarioId =
                    (String) query.getOutputParameterValue(
                            "p_inventario_id"
                    );

            Integer stockFisico =
                    (Integer) query.getOutputParameterValue(
                            "p_stock_fisico"
                    );

            Integer stockReservado =
                    (Integer) query.getOutputParameterValue(
                            "p_stock_reservado"
                    );

            int stockDisponible =
                    stockFisico - stockReservado;

            return new ResultadoMovimientoResponse(
                    inventarioId,
                    stockFisico,
                    stockReservado,
                    stockDisponible
            );

        } catch (PersistenceException ex) {
            throw new MovimientoInvalidoException(
                    extraerMensajeDelSP(ex)
            );
        }
    }

    private String extraerMensajeDelSP(Throwable ex) {

        Throwable actual = ex;

        while (actual.getCause() != null) {
            actual = actual.getCause();
        }

        return actual.getMessage() != null
                ? actual.getMessage()
                : "No se pudo registrar el movimiento";
    }

    private InventarioResponse toDTO(
            Inventario inv,
            Map<String, Long> stockTotalPorProducto) {

        ProductoResumenDTO productoDTO =
                new ProductoResumenDTO(
                        inv.getProducto().getId(),
                        inv.getProducto().getSku(),
                        inv.getProducto().getNombre(),
                        inv.getProducto().getStockMinimo()
                );

        AlmacenResumenDTO almacenDTO =
                new AlmacenResumenDTO(
                        inv.getAlmacen().getId(),
                        inv.getAlmacen().getNombre()
                );

        int stockDisponible =
                inv.getStockFisico() - inv.getStockReservado();

        long totalProducto =
                stockTotalPorProducto.getOrDefault(
                        inv.getProducto().getId(),
                        0L
                );

        String estado;

        if (totalProducto <= 0) {
            estado = "SIN_STOCK";
        } else if (totalProducto < inv.getProducto().getStockMinimo()) {
            estado = "BAJO_STOCK";
        } else {
            estado = "NORMAL";
        }

        return new InventarioResponse(
                inv.getId(),
                productoDTO,
                almacenDTO,
                inv.getStockFisico(),
                inv.getStockReservado(),
                stockDisponible,
                estado
        );
    }

    private MovimientoResponse toMovimientoDTO(
            MovimientoInventario mov) {

        String nombreUsuario =
                mov.getUsuario() != null
                        ? mov.getUsuario().getNombre()
                        : null;

        return new MovimientoResponse(
                mov.getId(),
                mov.getTipo().name(),
                mov.getCantidad(),
                mov.getMotivo(),
                nombreUsuario,
                mov.getCreatedAt()
        );
    }
}
