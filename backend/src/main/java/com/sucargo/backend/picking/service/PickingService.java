package com.sucargo.backend.picking.service;

import com.sucargo.backend.auth.entity.Usuario;
import com.sucargo.backend.auth.repository.UsuarioRepository;
import com.sucargo.backend.inventario.entity.Inventario;
import com.sucargo.backend.inventario.repository.InventarioRepository;
import com.sucargo.backend.pedido.entity.Pedido;
import com.sucargo.backend.pedido.entity.PedidoDetalle;
import com.sucargo.backend.pedido.repository.PedidoRepository;
import com.sucargo.backend.picking.dto.*;
import com.sucargo.backend.picking.entity.Picking;
import com.sucargo.backend.picking.entity.PickingDetalle;
import com.sucargo.backend.picking.repository.PickingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PickingService {

    private final PickingRepository pickingRepository;
    private final PedidoRepository pedidoRepository;
    private final InventarioRepository inventarioRepository;
    private final UsuarioRepository usuarioRepository;

    public List<PickingListResponse> listar(
            String empresaId,
            String estadoFiltro) {

        Picking.Estado estado =
                (estadoFiltro != null && !estadoFiltro.isBlank())
                        ? Picking.Estado.valueOf(estadoFiltro)
                        : null;

        return pickingRepository.buscar(empresaId, estado)
                .stream()
                .map(this::toListDTO)
                .toList();
    }

    public PickingDetailResponse obtener(
            String id,
            String empresaId) {

        Picking picking = buscarPickingDeEmpresa(id, empresaId);

        return toDetailDTO(picking);
    }

    @Transactional
    public PickingDetailResponse generar(
            GenerarPickingRequest request,
            String empresaId) {

        Pedido pedido = pedidoRepository
                .findByIdAndEmpresaId(
                        request.pedidoId(),
                        empresaId)
                .orElseThrow(() ->
                        new PickingInvalidoException(
                                "El pedido no existe o no pertenece a la empresa"));

        if (pedido.getEstado() != Pedido.Estado.STOCK_RESERVADO) {
            throw new PickingInvalidoException(
                    "El pedido debe estar en STOCK_RESERVADO para generar su picking");
        }

        if (pickingRepository.existsByPedidoIdAndEmpresaId(
        pedido.getId(),
        empresaId)) {

        throw new PickingInvalidoException(
            "Ya existe un picking para este pedido");
}

        if (pedido.getAlmacen() == null) {
            throw new PickingInvalidoException(
                    "El pedido no tiene un almacén asignado");
        }

        String almacenId = pedido.getAlmacen().getId();

        Picking picking = Picking.builder()
                .pedido(pedido)
                .estado(Picking.Estado.PENDIENTE)
                .build();

        List<PickingDetalle> lineas = new ArrayList<>();

        for (PedidoDetalle detalle : pedido.getItems()) {

            Inventario inventario = inventarioRepository
                    .listar(
                            empresaId,
                            almacenId,
                            detalle.getProducto().getId(),
                            null
                    )
                    .stream()
                    .findFirst()
                    .orElseThrow(() ->
                            new PickingInvalidoException(
                                    "No se encontró inventario para el producto "
                                            + detalle.getProducto().getNombre()
                                            + " en el almacén del pedido"));

            if (!pedido.getAlmacen().getId()
                    .equals(inventario.getAlmacen().getId())) {

                throw new PickingInvalidoException(
                        "Inconsistencia detectada: el inventario no pertenece "
                                + "al almacén asignado al pedido");
            }

            PickingDetalle linea = PickingDetalle.builder()
                    .picking(picking)
                    .pedidoDetalle(detalle)
                    .cantidadEsperada(detalle.getCantidad())
                    .cantidadRecolectada(0)
                    .build();

            lineas.add(linea);
        }

        picking.setLineas(lineas);

        picking = pickingRepository.save(picking);

        return toDetailDTO(picking);
    }

    @Transactional
    public PickingDetailResponse iniciar(
            String id,
            @NonNull String usuarioId,
            String empresaId) {

        Picking picking = buscarPickingDeEmpresa(id, empresaId);

        if (picking.getEstado() != Picking.Estado.PENDIENTE) {
            throw new PickingInvalidoException(
                    "El picking debe estar en PENDIENTE para iniciarse");
        }

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() ->
                        new PickingInvalidoException(
                                "Usuario no encontrado"));

        picking.setEstado(Picking.Estado.EN_PROCESO);
        picking.setUsuario(usuario);
        picking.setFechaInicio(LocalDateTime.now());

        pickingRepository.save(picking);

        return toDetailDTO(picking);
    }

    @Transactional
    public PickingLineaDTO registrarLinea(
            String pickingId,
            String lineaId,
            RegistrarLineaRequest request,
            String empresaId) {

        Picking picking =
                buscarPickingDeEmpresa(pickingId, empresaId);

        if (picking.getEstado() != Picking.Estado.EN_PROCESO) {
            throw new PickingInvalidoException(
                    "El picking debe estar EN_PROCESO para registrar líneas");
        }

        if (request.cantidadRecolectada() == null
                || request.cantidadRecolectada() < 0) {

            throw new PickingInvalidoException(
                    "La cantidad recolectada no puede ser negativa o nula");
        }

        PickingDetalle linea = picking.getLineas()
                .stream()
                .filter(l -> l.getId().equals(lineaId))
                .findFirst()
                .orElseThrow(() ->
                        new PickingInvalidoException(
                                "La línea no pertenece a este picking"));

        if (request.cantidadRecolectada()
                > linea.getCantidadEsperada()) {

            throw new PickingInvalidoException(
                    "La cantidad recolectada no puede superar "
                            + "la cantidad esperada");
        }

        linea.setCantidadRecolectada(
                request.cantidadRecolectada());

        linea.setIncidencia(
                request.incidencia());

        pickingRepository.save(picking);

        return toLineaDTO(linea);
    }

    @Transactional
    public CompletarPickingResponse completar(
            String id,
            String empresaId) {

        Picking picking =
                buscarPickingDeEmpresa(id, empresaId);

        if (picking.getEstado() != Picking.Estado.EN_PROCESO) {
            throw new PickingInvalidoException(
                    "El picking debe estar EN_PROCESO para completarse");
        }

        boolean faltaAlguna =
                picking.getLineas()
                        .stream()
                        .anyMatch(l ->
                                l.getCantidadRecolectada() == null);

        if (faltaAlguna) {
            throw new PickingInvalidoException(
                    "Todas las líneas deben tener una cantidad recolectada registrada");
        }

        boolean hayIncidencias =
                picking.getLineas()
                        .stream()
                        .anyMatch(l ->
                                (l.getIncidencia() != null
                                        && !l.getIncidencia().isBlank())
                                        || !Objects.equals(
                                                l.getCantidadRecolectada(),
                                                l.getCantidadEsperada()));

        picking.setEstado(
                hayIncidencias
                        ? Picking.Estado.COMPLETADO_CON_INCIDENCIAS
                        : Picking.Estado.COMPLETADO);

        picking.setFechaFin(LocalDateTime.now());

        pickingRepository.save(picking);

        Pedido pedido = picking.getPedido();

        pedido.setEstado(
                Pedido.Estado.PICKING_COMPLETADO);

        pedidoRepository.save(pedido);

        return new CompletarPickingResponse(
                picking.getId(),
                picking.getEstado().name(),
                pedido.getEstado().name());
    }

    private Picking buscarPickingDeEmpresa(
            String id,
            String empresaId) {

        return pickingRepository
                .findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(
                        PickingNoEncontradoException::new);
    }

    private PickingListResponse toListDTO(
            Picking picking) {

        return new PickingListResponse(
                picking.getId(),
                new PedidoResumenDTO(
                        picking.getPedido().getId(),
                        picking.getPedido().getCodigo()),
                picking.getEstado().name(),
                picking.getUsuario() != null
                        ? picking.getUsuario().getNombre()
                        : null,
                picking.getFechaInicio(),
                picking.getFechaFin()
        );
    }

    private PickingDetailResponse toDetailDTO(
            Picking picking) {

        List<PickingLineaDTO> lineas =
                picking.getLineas()
                        .stream()
                        .map(this::toLineaDTO)
                        .toList();

        return new PickingDetailResponse(
                picking.getId(),
                new PedidoResumenDTO(
                        picking.getPedido().getId(),
                        picking.getPedido().getCodigo()),
                picking.getEstado().name(),
                picking.getUsuario() != null
                        ? picking.getUsuario().getNombre()
                        : null,
                picking.getFechaInicio(),
                picking.getFechaFin(),
                lineas
        );
    }

    private PickingLineaDTO toLineaDTO(
            PickingDetalle linea) {

        var producto =
                linea.getPedidoDetalle().getProducto();

        return new PickingLineaDTO(
                linea.getId(),
                new ProductoResumenDTO(
                        producto.getId(),
                        producto.getSku(),
                        producto.getNombre()),
                linea.getCantidadEsperada(),
                linea.getCantidadRecolectada(),
                linea.getIncidencia()
        );
    }
}