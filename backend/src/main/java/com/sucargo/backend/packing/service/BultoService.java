package com.sucargo.backend.packing.service;

import com.sucargo.backend.auth.entity.Usuario;
import com.sucargo.backend.auth.repository.UsuarioRepository;
import com.sucargo.backend.pedido.entity.Pedido;
import com.sucargo.backend.pedido.repository.PedidoRepository;
import com.sucargo.backend.picking.entity.Picking;
import com.sucargo.backend.picking.repository.PickingRepository;
import com.sucargo.backend.packing.dto.AgregarProductoRequest;
import com.sucargo.backend.packing.dto.BultoDetailResponse;
import com.sucargo.backend.packing.dto.BultoListResponse;
import com.sucargo.backend.packing.dto.BultoProductoDTO;
import com.sucargo.backend.packing.dto.CompletarPackingResponse;
import com.sucargo.backend.packing.dto.CrearBultoRequest;
import com.sucargo.backend.packing.dto.PedidoPackingProductoDTO;
import com.sucargo.backend.packing.dto.RegistrarPesoRequest;
import com.sucargo.backend.packing.entity.Bulto;
import com.sucargo.backend.packing.entity.BultoDetalle;
import com.sucargo.backend.packing.repository.BultoRepository;
import com.sucargo.backend.producto.entity.Producto;
import com.sucargo.backend.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sucargo.backend.packing.dto.CompletarPackingResponse;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BultoService {

    private final BultoRepository bultoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PickingRepository pickingRepository;

    // =========================================================
    // LISTAR BULTOS DE UN PEDIDO
    // =========================================================

    public List<BultoListResponse> listar(
            String pedidoId,
            String empresaId) {

        Pedido pedido = pedidoRepository
                .findByIdAndEmpresaId(pedidoId, empresaId)
                .orElseThrow(() ->
                        new RuntimeException("Pedido no encontrado"));

        List<Bulto> bultos = bultoRepository
                .findByPedidoIdAndEmpresaIdOrderByNumeroAsc(
                        pedido.getId(),
                        empresaId
                );

        int totalBultos = bultos.size();

        return bultos.stream()
                .map(bulto -> toListResponse(
                        bulto,
                        totalBultos
                ))
                .toList();
    }

    // =========================================================
    // OBTENER BULTO
    // =========================================================

    public BultoDetailResponse obtener(
            String id,
            String empresaId) {

        Bulto bulto = buscarBultoDeEmpresa(
                id,
                empresaId
        );

        return toDetailResponse(
                bulto,
                empresaId
        );
    }

   @Transactional
public BultoDetailResponse crear(
        CrearBultoRequest request,
        String usuarioId,
        String empresaId) {

    Pedido pedido = pedidoRepository
            .findByIdAndEmpresaId(
                    request.pedidoId(),
                    empresaId
            )
            .orElseThrow(() ->
                    new RuntimeException(
                            "Pedido no encontrado"
                    ));

    if (pedido.getEstado() != Pedido.Estado.PICKING_COMPLETADO) {
        throw new RuntimeException(
                "El pedido debe tener el picking completado para crear un bulto"
        );
    }

    Usuario usuario = usuarioRepository
            .findByIdAndEmpresaId(
                    usuarioId,
                    empresaId
            )
            .orElseThrow(() ->
                    new RuntimeException(
                            "Usuario no encontrado"
                    ));

    long cantidadBultos =
            bultoRepository.countByPedidoIdAndEmpresaId(
                    pedido.getId(),
                    empresaId
            );

    int siguienteNumero =
            (int) cantidadBultos + 1;

    String codigoBulto = "BUL-" +
            UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();

    Bulto bulto = Bulto.builder()
            .pedido(pedido)
            .usuario(usuario)
            .codigo(codigoBulto)
            .numero(siguienteNumero)
            .estado(Bulto.Estado.ABIERTO)
            .build();

    bulto = bultoRepository.save(bulto);

    return toDetailResponse(
            bulto,
            empresaId
    );
}
    // =========================================================
    // AGREGAR PRODUCTO AL BULTO
    // =========================================================

    @Transactional
    public BultoProductoDTO agregarProducto(
            String bultoId,
            AgregarProductoRequest request,
            String empresaId) {

        Bulto bulto = buscarBultoDeEmpresa(
                bultoId,
                empresaId
        );

        if (bulto.getEstado() == Bulto.Estado.CERRADO) {
            throw new RuntimeException(
                    "No se pueden agregar productos a un bulto cerrado"
            );
        }

        Producto producto = productoRepository
                .findByIdAndEmpresaId(
                        request.productoId(),
                        empresaId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Producto no encontrado"
                        ));

        String pedidoId =
                bulto.getPedido().getId();

        int cantidadRecolectada =
                obtenerCantidadRecolectada(
                        pedidoId,
                        producto.getId(),
                        empresaId
                );

        int yaDistribuido =
                sumarDistribuidoEnBultos(
                        pedidoId,
                        producto.getId(),
                        empresaId
                );

        int nuevaCantidad =
                request.cantidad();

        /*
         * Si el producto ya existe dentro de ESTE bulto,
         * debemos descontar la cantidad actual de este mismo
         * bulto al calcular cuánto hay distribuido en OTROS.
         */
        BultoDetalle detalleExistente =
                bulto.getProductos()
                        .stream()
                        .filter(detalle ->
                                detalle.getProducto()
                                        .getId()
                                        .equals(producto.getId())
                        )
                        .findFirst()
                        .orElse(null);

        int cantidadActual =
                detalleExistente != null &&
                        detalleExistente.getCantidad() != null
                        ? detalleExistente.getCantidad()
                        : 0;

        int distribuidoEnOtrosBultos =
                yaDistribuido - cantidadActual;

        int cantidadFinal =
                cantidadActual + nuevaCantidad;

        if (distribuidoEnOtrosBultos + cantidadFinal
                > cantidadRecolectada) {

            throw new RuntimeException(
                    "La cantidad supera lo recolectado en picking. " +
                    "Recolectado: " + cantidadRecolectada +
                    ", distribuido en otros bultos: "
                    + distribuidoEnOtrosBultos
            );
        }

        BultoDetalle detalle;

        if (detalleExistente != null) {

            detalleExistente.setCantidad(
                    cantidadFinal
            );

            detalle = detalleExistente;

        } else {

            detalle = BultoDetalle.builder()
                    .bulto(bulto)
                    .producto(producto)
                    .cantidad(nuevaCantidad)
                    .build();

            bulto.getProductos().add(detalle);
        }

        bultoRepository.save(bulto);

        return toBultoProductoDTO(detalle);
    }

    // =========================================================
    // QUITAR PRODUCTO DEL BULTO
    // =========================================================

    @Transactional
    public void quitarProducto(
            String bultoId,
            String detalleId,
            String empresaId) {

        Bulto bulto = buscarBultoDeEmpresa(
                bultoId,
                empresaId
        );

        if (bulto.getEstado() == Bulto.Estado.CERRADO) {
            throw new RuntimeException(
                    "No se pueden quitar productos de un bulto cerrado"
            );
        }

        BultoDetalle detalle = bulto.getProductos()
                .stream()
                .filter(d ->
                        d.getId().equals(detalleId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Detalle de bulto no encontrado"
                        ));

        bulto.getProductos().remove(detalle);

        bultoRepository.save(bulto);
    }

    // =========================================================
    // REGISTRAR PESO
    // =========================================================

    @Transactional
    public BultoDetailResponse registrarPeso(
            String bultoId,
            RegistrarPesoRequest request,
            String empresaId) {

        Bulto bulto = buscarBultoDeEmpresa(
                bultoId,
                empresaId
        );

        if (bulto.getEstado() == Bulto.Estado.CERRADO) {
            throw new RuntimeException(
                    "No se puede modificar el peso de un bulto cerrado"
            );
        }

        if (request.peso() == null ||
                request.peso().compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "El peso debe ser mayor que cero"
            );
        }

        bulto.setPeso(request.peso());

        bultoRepository.save(bulto);

        return toDetailResponse(
                bulto,
                empresaId
        );
    }

    // =========================================================
    // CERRAR BULTO
    // =========================================================

    @Transactional
    public BultoDetailResponse cerrar(
            String bultoId,
            String empresaId) {

        Bulto bulto = buscarBultoDeEmpresa(
                bultoId,
                empresaId
        );

        if (bulto.getEstado() == Bulto.Estado.CERRADO) {
            throw new RuntimeException(
                    "El bulto ya está cerrado"
            );
        }

        if (bulto.getProductos() == null ||
                bulto.getProductos().isEmpty()) {

            throw new RuntimeException(
                    "No se puede cerrar un bulto vacío"
            );
        }

        bulto.setEstado(
                Bulto.Estado.CERRADO
        );

        bultoRepository.save(bulto);

        return toDetailResponse(
                bulto,
                empresaId
        );
    }

    // =========================================================
    // ELIMINAR BULTO
    // =========================================================

    @Transactional
    public void eliminar(
            String bultoId,
            String empresaId) {

        Bulto bulto = buscarBultoDeEmpresa(
                bultoId,
                empresaId
        );

        if (bulto.getEstado() == Bulto.Estado.CERRADO) {
            throw new RuntimeException(
                    "No se puede eliminar un bulto cerrado"
            );
        }

        bultoRepository.delete(bulto);
    }

    // =========================================================
    // COMPLETAR PACKING
    // =========================================================

    @Transactional
        public CompletarPackingResponse completarPacking(
        String pedidoId,
        String empresaId) {

        Pedido pedido = pedidoRepository
                .findByIdAndEmpresaId(
                        pedidoId,
                        empresaId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Pedido no encontrado"
                        ));

        List<Bulto> bultos =
                bultoRepository
                        .findByPedidoIdAndEmpresaIdOrderByNumeroAsc(
                                pedido.getId(),
                                empresaId
                        );

        if (bultos.isEmpty()) {
            throw new RuntimeException(
                    "El pedido debe tener al menos un bulto"
            );
        }

        boolean existeBultoAbierto =
                bultos.stream()
                        .anyMatch(b ->
                                b.getEstado()
                                        == Bulto.Estado.ABIERTO
                        );

        if (existeBultoAbierto) {
            throw new RuntimeException(
                    "Todos los bultos deben estar cerrados"
            );
        }

        String pickingId =
                obtenerPickingIdDelPedido(pedidoId, empresaId);

        Picking picking =
                pickingRepository
                        .findByIdAndEmpresaId(
                                pickingId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Picking no encontrado"
                                ));

        Map<String, Integer> recolectadoPorProducto =
                picking.getLineas()
                        .stream()
                        .collect(Collectors.toMap(
                                linea ->
                                        linea.getPedidoDetalle()
                                                .getProducto()
                                                .getId(),

                                linea ->
                                        linea.getCantidadRecolectada()
                                                != null
                                                ? linea.getCantidadRecolectada()
                                                : 0,

                                Integer::sum
                        ));

        Map<String, Integer> distribuidoPorProducto =
                new HashMap<>();

        for (Bulto bulto : bultos) {

            for (BultoDetalle detalle :
                    bulto.getProductos()) {

                String productoId =
                        detalle.getProducto().getId();

                distribuidoPorProducto.merge(
                        productoId,
                        detalle.getCantidad(),
                        Integer::sum
                );
            }
        }

        for (Map.Entry<String, Integer> entry :
                recolectadoPorProducto.entrySet()) {

            String productoId =
                    entry.getKey();

            int recolectado =
                    entry.getValue();

            int distribuido =
                    distribuidoPorProducto
                            .getOrDefault(
                                    productoId,
                                    0
                            );

            if (distribuido != recolectado) {
                throw new RuntimeException(
                        "El producto " + productoId +
                        " no tiene la cantidad completa distribuida. " +
                        "Recolectado: " + recolectado +
                        ", distribuido: " + distribuido
                );
            }
        }

        for (Map.Entry<String, Integer> entry :
                distribuidoPorProducto.entrySet()) {

            String productoId =
                    entry.getKey();

            int distribuido =
                    entry.getValue();

            int recolectado =
                    recolectadoPorProducto
                            .getOrDefault(
                                    productoId,
                                    0
                            );

            if (distribuido != recolectado) {
                throw new RuntimeException(
                        "Existe una cantidad distribuida " +
                        "que no corresponde al picking"
                );
            }
        }

        pedido.setEstado(
        Pedido.Estado.PACKING_COMPLETADO
        );

        pedidoRepository.save(pedido);

        return new CompletarPackingResponse(
        pedido.getId(),
        pedido.getEstado().name(),
        bultos.size()
        );
    }

    // =========================================================
    // PRODUCTOS DEL PEDIDO PARA PACKING
    // =========================================================

    public List<PedidoPackingProductoDTO> obtenerProductosDelPedido(
            String pedidoId,
            String empresaId) {

        Pedido pedido = pedidoRepository
                .findByIdAndEmpresaId(
                        pedidoId,
                        empresaId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Pedido no encontrado"
                        ));

        String pickingId =
        obtenerPickingIdDelPedido(
                pedido.getId(),
                empresaId
        );

        Picking picking =
                pickingRepository
                        .findByIdAndEmpresaId(
                                pickingId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Picking no encontrado"
                                ));

        List<Bulto> bultos =
                bultoRepository
                        .findByPedidoIdAndEmpresaIdOrderByNumeroAsc(
                                pedido.getId(),
                                empresaId
                        );

        Map<String, Integer> distribuido =
                new HashMap<>();

        for (Bulto bulto : bultos) {

            for (BultoDetalle detalle :
                    bulto.getProductos()) {

                String productoId =
                        detalle.getProducto().getId();

                distribuido.merge(
                        productoId,
                        detalle.getCantidad(),
                        Integer::sum
                );
            }
        }

        return picking.getLineas()
                .stream()
                .map(linea -> {

                    Producto producto =
                            linea.getPedidoDetalle()
                                    .getProducto();

                    int cantidadSolicitada =
                            linea.getPedidoDetalle()
                                    .getCantidad();

                    int cantidadRecolectada =
                            linea.getCantidadRecolectada()
                                    != null
                                    ? linea.getCantidadRecolectada()
                                    : 0;

                    int cantidadEmpaquetada =
                            distribuido.getOrDefault(
                                    producto.getId(),
                                    0
                            );

                    int cantidadPendiente =
                            Math.max(
                                    cantidadRecolectada
                                            - cantidadEmpaquetada,
                                    0
                            );

                    /*
                     * El DTO contempla pesoUnitario y
                     * pesoEstimadoPendiente, pero el modelo
                     * actual de Producto no fue definido aquí.
                     * Por eso no inventamos un campo.
                     */
                    BigDecimal pesoUnitario = null;

                    BigDecimal pesoEstimadoPendiente = null;

                    return new PedidoPackingProductoDTO(
                            producto.getId(),
                            producto.getSku(),
                            producto.getNombre(),
                            cantidadSolicitada,
                            cantidadRecolectada,
                            cantidadEmpaquetada,
                            cantidadPendiente,
                            pesoUnitario,
                            pesoEstimadoPendiente
                    );
                })
                .toList();
    }

    // =========================================================
    // BUSCAR BULTO DE LA EMPRESA
    // =========================================================

    private Bulto buscarBultoDeEmpresa(
            String id,
            String empresaId) {

        return bultoRepository
                .findByIdAndEmpresaId(
                        id,
                        empresaId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bulto no encontrado"
                        ));
    }

    // =========================================================
    // CONVERTIR A LIST RESPONSE
    // =========================================================

    private BultoListResponse toListResponse(
            Bulto bulto,
            int totalBultos) {

        return new BultoListResponse(
                bulto.getId(),
                bulto.getCodigo(),
                bulto.getNumero(),
                totalBultos,
                bulto.getPeso(),
                bulto.getEstado().name()
        );
    }

    // =========================================================
    // CONVERTIR A DETAIL RESPONSE
    // =========================================================

    private BultoDetailResponse toDetailResponse(
            Bulto bulto,
            String empresaId) {

        int totalBultos =
                (int) bultoRepository
                        .countByPedidoIdAndEmpresaId(
                                bulto.getPedido().getId(),
                                empresaId
                        );

        List<BultoProductoDTO> productos =
                bulto.getProductos()
                        .stream()
                        .map(this::toBultoProductoDTO)
                        .toList();

        String nombreUsuario =
                bulto.getUsuario() != null
                        ? bulto.getUsuario().getNombre()
                        : null;

        return new BultoDetailResponse(
                bulto.getId(),
                bulto.getCodigo(),
                bulto.getNumero(),
                totalBultos,
                bulto.getPeso(),
                bulto.getEstado().name(),
                nombreUsuario,
                productos
        );
    }

    // =========================================================
    // CONVERTIR PRODUCTO DEL BULTO
    // =========================================================

    private BultoProductoDTO toBultoProductoDTO(
            BultoDetalle detalle) {

        return new BultoProductoDTO(
                detalle.getProducto().getId(),
                detalle.getProducto().getSku(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad()
        );
    }

    // =========================================================
    // OBTENER CANTIDAD RECOLECTADA
    // =========================================================

    private int obtenerCantidadRecolectada(
            String pedidoId,
            String productoId,
            String empresaId) {

        String pickingId =
                obtenerPickingIdDelPedido(
                pedidoId,
                empresaId
                );

        return pickingRepository
                .findByIdAndEmpresaId(
                        pickingId,
                        empresaId
                )
                .flatMap(picking ->
                        picking.getLineas()
                                .stream()
                                .filter(linea ->
                                        linea.getPedidoDetalle()
                                                .getProducto()
                                                .getId()
                                                .equals(productoId)
                                )
                                .findFirst()
                )
                .map(linea ->
                        linea.getCantidadRecolectada()
                                != null
                                ? linea.getCantidadRecolectada()
                                : 0
                )
                .orElse(0);
    }

    // =========================================================
    // SUMAR LO DISTRIBUIDO EN TODOS LOS BULTOS
    // =========================================================

    private int sumarDistribuidoEnBultos(
            String pedidoId,
            String productoId,
            String empresaId) {

        List<Bulto> bultos =
                bultoRepository
                        .findByPedidoIdAndEmpresaIdOrderByNumeroAsc(
                                pedidoId,
                                empresaId
                        );

        return bultos.stream()
                .flatMap(bulto ->
                        bulto.getProductos().stream()
                )
                .filter(detalle ->
                        detalle.getProducto()
                                .getId()
                                .equals(productoId)
                )
                .mapToInt(BultoDetalle::getCantidad)
                .sum();
    }

    // =========================================================
    // OBTENER PICKING DEL PEDIDO
    // =========================================================

    private String obtenerPickingIdDelPedido(
        String pedidoId,
        String empresaId) {

    return pickingRepository
            .findPickingIdByPedidoIdAndEmpresaId(
                    pedidoId,
                    empresaId
            )
            .orElseThrow(() ->
                    new RuntimeException(
                            "El pedido no tiene picking"
                    ));
}
}
