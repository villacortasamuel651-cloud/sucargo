package com.sucargo.backend.pedido.service;

import com.sucargo.backend.almacen.entity.Almacen;
import com.sucargo.backend.almacen.repository.AlmacenRepository;
import com.sucargo.backend.almacen.service.AlmacenNoEncontradoException;
import com.sucargo.backend.auth.entity.Usuario;
import com.sucargo.backend.auth.repository.UsuarioRepository;
import com.sucargo.backend.cliente.entity.Cliente;
import com.sucargo.backend.cliente.entity.PuntoEntrega;
import com.sucargo.backend.cliente.repository.ClienteRepository;
import com.sucargo.backend.cliente.repository.PuntoEntregaRepository;
import com.sucargo.backend.inventario.entity.Inventario;
import com.sucargo.backend.inventario.repository.InventarioRepository;
import com.sucargo.backend.inventario.service.InventarioService;
import com.sucargo.backend.pedido.dto.*;
import com.sucargo.backend.pedido.entity.Pedido;
import com.sucargo.backend.pedido.entity.PedidoDetalle;
import com.sucargo.backend.pedido.repository.PedidoRepository;
import com.sucargo.backend.producto.entity.Producto;
import com.sucargo.backend.producto.repository.ProductoRepository;
import com.sucargo.backend.producto.service.ProductoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sucargo.backend.almacen.dto.AlmacenResumenDTO;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final PuntoEntregaRepository puntoEntregaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioRepository inventarioRepository;
    private final InventarioService inventarioService;
    private final AlmacenRepository almacenRepository;

    public List<PedidoListResponse> listar(
            String empresaId, String estadoFiltro, String clienteId, String texto) {

        Pedido.Estado estado = (estadoFiltro != null && !estadoFiltro.isBlank())
                ? Pedido.Estado.valueOf(estadoFiltro)
                : null;

        return pedidoRepository.buscar(
                        empresaId,
                        estado,
                        clienteId,
                        (texto != null && !texto.isBlank()) ? texto : null
                )
                .stream()
                .map(this::toListDTO)
                .toList();
    }

    public List<PedidoListResponse> listarDisponiblesParaDistribucion(
            String empresaId) {

        return pedidoRepository.buscarDisponiblesParaDistribucion(empresaId)
                .stream()
                .map(this::toListDTO)
                .toList();
    }

    public PedidoDetailResponse obtener(String id, String empresaId) {
        Pedido pedido = buscarPedidoDeEmpresa(id, empresaId);
        return toDetailDTO(pedido);
    }

    /*
     * El SP de inventario hace COMMIT por cada movimiento.
     * Por eso no dependemos del rollback de la transacción de este service.
     * Si una reserva falla después de otras reservas exitosas,
     * liberamos manualmente las reservas realizadas.
     */
    @SuppressWarnings("null")
    @Transactional
    public PedidoDetailResponse crear(
            PedidoRequest request,
            String usuarioId,
            String empresaId) {

        Cliente cliente = clienteRepository
                .findByIdAndEmpresaId(request.clienteId(), empresaId)
                .orElseThrow(() -> new PedidoInvalidoException(
                        "El cliente no existe o no pertenece a la empresa"));

        PuntoEntrega puntoEntrega = puntoEntregaRepository
                .findByIdAndClienteId(
                        request.puntoEntregaId(),
                        cliente.getId())
                .orElseThrow(() -> new PedidoInvalidoException(
                        "El punto de entrega no existe o no pertenece a este cliente"));

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() -> new PedidoInvalidoException(
                        "Usuario no encontrado"));

        /*
         * El almacén seleccionado debe pertenecer a la misma empresa.
         */
        Almacen almacen = almacenRepository
                .findByIdAndEmpresaId(
                        request.almacenId(),
                        empresaId)
                .orElseThrow(AlmacenNoEncontradoException::new);

        Pedido.Prioridad prioridad =
                (request.prioridad() != null
                        && !request.prioridad().isBlank())
                        ? Pedido.Prioridad.valueOf(request.prioridad())
                        : Pedido.Prioridad.MEDIA;

        /*
         * 1. Crear pedido asociado al almacén seleccionado.
         */
        Pedido pedido = Pedido.builder()
                .empresaId(empresaId)
                .cliente(cliente)
                .puntoEntrega(puntoEntrega)
                .usuario(usuario)
                .almacen(almacen)
                .estado(Pedido.Estado.CREADO)
                .prioridad(prioridad)
                .build();

        pedido = pedidoRepository.save(pedido);

        /*
         * 2. Generar código legible.
         */
        pedido.setCodigo(
                "PED-"
                        + pedido.getId()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase()
        );

        /*
         * 3. Crear las líneas y validar que los productos
         *    pertenezcan a la empresa.
         */
        List<PedidoDetalle> detalles = new ArrayList<>();

        for (PedidoItemDTO item : request.items()) {

            Producto producto = productoRepository
                    .findByIdAndEmpresaId(
                            item.productoId(),
                            empresaId)
                    .orElseThrow(ProductoNoEncontradoException::new);

            PedidoDetalle detalle = PedidoDetalle.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(item.cantidad())
                    .cantidadReservada(0)
                    .build();

            detalles.add(detalle);
        }

        pedido.setItems(detalles);
        pedidoRepository.save(pedido);

        /*
         * 4. PRIMERA VALIDACIÓN:
         *
         * Comprobamos que el almacén seleccionado pueda
         * atender TODO el pedido.
         *
         * No buscamos otro almacén.
         * No dividimos el pedido.
         */
        for (PedidoDetalle detalle : detalles) {

            boolean tieneStock = tieneStockSuficiente(
                    detalle.getProducto().getId(),
                    almacen.getId(),
                    empresaId,
                    detalle.getCantidad()
            );

            if (!tieneStock) {
                pedido.setEstado(Pedido.Estado.SIN_STOCK);
                pedidoRepository.save(pedido);

                return toDetailDTO(pedido);
            }
        }

        /*
         * 5. SEGUNDA FASE:
         *
         * Todas las líneas tienen stock disponible en el mismo almacén.
         * Ahora realizamos las reservas.
         */
        List<PedidoDetalle> reservadasConExito = new ArrayList<>();

        for (PedidoDetalle detalle : detalles) {

            try {

                inventarioService.registrarMovimientoInterno(
                        detalle.getProducto().getId(),
                        almacen.getId(),
                        "RESERVA",
                        detalle.getCantidad(),
                        usuarioId,
                        "Reserva por pedido " + pedido.getCodigo()
                );

                detalle.setCantidadReservada(
                        detalle.getCantidad()
                );

                reservadasConExito.add(detalle);

            } catch (Exception ex) {

                /*
                 * Puede ocurrir que entre la validación y la reserva
                 * otro proceso haya tomado el stock.
                 *
                 * Liberamos las reservas realizadas anteriormente.
                 */
                liberarLineas(
                        reservadasConExito,
                        almacen.getId(),
                        usuarioId
                );

                pedido.setEstado(Pedido.Estado.SIN_STOCK);
                pedidoRepository.save(pedido);

                return toDetailDTO(pedido);
            }
        }

        /*
         * 6. Todas las líneas fueron reservadas correctamente.
         */
        pedido.setEstado(Pedido.Estado.STOCK_RESERVADO);
        pedidoRepository.save(pedido);

        return toDetailDTO(pedido);
    }

    @Transactional
    public PedidoDetailResponse cancelar(
            String id,
            String usuarioId,
            String empresaId) {

        Pedido pedido = buscarPedidoDeEmpresa(id, empresaId);

        boolean cancelable =
                pedido.getEstado() == Pedido.Estado.CREADO
                        || pedido.getEstado() == Pedido.Estado.STOCK_RESERVADO
                        || pedido.getEstado() == Pedido.Estado.SIN_STOCK;

        if (!cancelable) {
            throw new PedidoNoCancelableException();
        }

        /*
         * Si tenía stock reservado, liberarlo del almacén
         * asociado al pedido.
         */
        if (pedido.getEstado() == Pedido.Estado.STOCK_RESERVADO) {

            liberarLineas(
                    pedido.getItems(),
                    pedido.getAlmacen().getId(),
                    usuarioId
            );
        }

        pedido.setEstado(Pedido.Estado.CANCELADO);
        pedidoRepository.save(pedido);

        return toDetailDTO(pedido);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    /**
     * Verifica si un producto tiene suficiente stock disponible
     * en un almacén específico.
     *
     * Disponible = stock físico - stock reservado.
     */
    private boolean tieneStockSuficiente(
            String productoId,
            String almacenId,
            String empresaId,
            int cantidadNecesaria) {

        List<Inventario> inventarios =
                inventarioRepository.listar(
                        empresaId,
                        almacenId,
                        productoId,
                        null
                );

        return inventarios.stream()
                .anyMatch(inv ->
                        (inv.getStockFisico() - inv.getStockReservado())
                                >= cantidadNecesaria
                );
    }

    /**
     * Libera las reservas realizadas en un pedido.
     *
     * Todas las liberaciones utilizan el mismo almacén
     * asociado al pedido.
     */
    private void liberarLineas(
            List<PedidoDetalle> detalles,
            String almacenId,
            String usuarioId) {

        for (PedidoDetalle detalle : detalles) {

            if (detalle.getCantidadReservada() != null
                    && detalle.getCantidadReservada() > 0) {

                try {

                    inventarioService.registrarMovimientoInterno(
                            detalle.getProducto().getId(),
                            almacenId,
                            "LIBERACION",
                            detalle.getCantidadReservada(),
                            usuarioId,
                            "Liberación automática de reserva del pedido"
                    );

                    detalle.setCantidadReservada(0);

                } catch (Exception ex) {
                    /*
                     * No detenemos todo el flujo por una liberación fallida.
                     * En producción esto debería registrarse para revisión.
                     */
                }
            }
        }
    }

    private Pedido buscarPedidoDeEmpresa(
            String id,
            String empresaId) {

        return pedidoRepository
                .findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(PedidoNoEncontradoException::new);
    }

    private PedidoListResponse toListDTO(Pedido pedido) {

        return new PedidoListResponse(
                pedido.getId(),
                pedido.getCodigo(),

                new ClienteResumenDTO(
                        pedido.getCliente().getId(),
                        pedido.getCliente().getRazonSocial()
                ),

                new AlmacenResumenDTO(
                        pedido.getAlmacen().getId(),
                        pedido.getAlmacen().getNombre()
                ),

                pedido.getEstado().name(),
                pedido.getPrioridad().name(),
                pedido.getItems().size(),
                pedido.getCreatedAt()
        );
    }

    private PedidoDetailResponse toDetailDTO(Pedido pedido) {

        List<PedidoItemResponse> items =
                pedido.getItems()
                        .stream()
                        .map(d -> new PedidoItemResponse(
                                d.getId(),

                                new ProductoResumenDTO(
                                        d.getProducto().getId(),
                                        d.getProducto().getSku(),
                                        d.getProducto().getNombre()
                                ),

                                d.getCantidad(),
                                d.getCantidadReservada()
                        ))
                        .toList();

        return new PedidoDetailResponse(
                pedido.getId(),
                pedido.getCodigo(),

                new ClienteResumenDTO(
                        pedido.getCliente().getId(),
                        pedido.getCliente().getRazonSocial()
                ),

                new PuntoEntregaResumenDTO(
                        pedido.getPuntoEntrega().getId(),
                        pedido.getPuntoEntrega().getDireccion(),
                        pedido.getPuntoEntrega().getDistrito()
                ),

                new AlmacenResumenDTO(
                        pedido.getAlmacen().getId(),
                        pedido.getAlmacen().getNombre()
                ),

                pedido.getEstado().name(),
                pedido.getPrioridad().name(),
                pedido.getUsuario().getNombre(),
                pedido.getCreatedAt(),
                items
        );
    }

    public List<AlmacenResumenDTO> obtenerAlmacenesDisponibles(
        AlmacenesDisponiblesRequest request,
        String empresaId) {

    List<Almacen> almacenes =
            almacenRepository.findByEmpresaIdAndEstado(
                    empresaId,
                    Almacen.Estado.ACTIVO
            );

    List<AlmacenResumenDTO> disponibles = new ArrayList<>();

    for (Almacen almacen : almacenes) {

        boolean puedeAtenderTodo = true;

        for (PedidoItemDTO item : request.items()) {

            boolean tieneStock = tieneStockSuficiente(
                    item.productoId(),
                    almacen.getId(),
                    empresaId,
                    item.cantidad()
            );

            if (!tieneStock) {
                puedeAtenderTodo = false;
                break;
            }
        }

        if (puedeAtenderTodo) {
            disponibles.add(
                    new AlmacenResumenDTO(
                            almacen.getId(),
                            almacen.getNombre()
                    )
            );
        }
    }

    return disponibles;
}












}