package com.sucargo.backend.ordentransporte.service;

import com.sucargo.backend.ordentransporte.dto.*;
import com.sucargo.backend.ordentransporte.entity.OrdenTransporte;
import com.sucargo.backend.ordentransporte.repository.OrdenTransporteRepository;
import com.sucargo.backend.packing.entity.Bulto;
import com.sucargo.backend.packing.repository.BultoRepository;
import com.sucargo.backend.distribucion.entity.Distribucion;
import com.sucargo.backend.distribucion.repository.DistribucionRepository;
import com.sucargo.backend.distribucion.service.DistribucionInvalidaException;
import com.sucargo.backend.pedido.entity.Pedido;
import com.sucargo.backend.pedido.repository.PedidoRepository;
import com.sucargo.backend.almacen.entity.Almacen;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrdenTransporteService {

    private final OrdenTransporteRepository ordenTransporteRepository;
    private final DistribucionRepository distribucionRepository;
    private final PedidoRepository pedidoRepository;
    private final BultoRepository bultoRepository;

    public OrdenTransporteService(
            OrdenTransporteRepository ordenTransporteRepository,
            DistribucionRepository distribucionRepository,
            PedidoRepository pedidoRepository,
            BultoRepository bultoRepository) {

        this.ordenTransporteRepository = ordenTransporteRepository;
        this.distribucionRepository = distribucionRepository;
        this.pedidoRepository = pedidoRepository;
        this.bultoRepository = bultoRepository;
    }

    // =========================================================
    // LISTAR ÓRDENES
    // =========================================================

    @Transactional(readOnly = true)
    public List<OrdenTransporteListResponse> listarOrdenes(
            String distribucionId,
            String estado,
            String empresaId) {

        List<OrdenTransporte> lista;

        if (distribucionId != null && estado != null) {

            lista = ordenTransporteRepository
                    .findByDistribucionIdAndEstadoAndEmpresaId(
                            distribucionId,
                            estado,
                            empresaId
                    );

        } else if (distribucionId != null) {

            lista = ordenTransporteRepository
                    .findByDistribucionIdAndEmpresaId(
                            distribucionId,
                            empresaId
                    );

        } else if (estado != null) {

            lista = ordenTransporteRepository
                    .findByEstadoAndEmpresaId(
                            estado,
                            empresaId
                    );

        } else {

            lista = ordenTransporteRepository
                    .findAllByEmpresaId(empresaId);
        }

        List<OrdenTransporteListResponse> respuesta =
                new ArrayList<>();

        for (OrdenTransporte ot : lista) {

            String destinoStr = null;

            if (ot.getPedido() != null
                    && ot.getPedido().getPuntoEntrega() != null) {

                destinoStr =
                        ot.getPedido()
                                .getPuntoEntrega()
                                .getDireccion()
                                + " — "
                                + ot.getPedido()
                                .getPuntoEntrega()
                                .getDistrito();
            }

            String pedidoCodigo = null;

            if (ot.getPedido() != null) {
                pedidoCodigo = ot.getPedido().getCodigo();
            }

            respuesta.add(
                    new OrdenTransporteListResponse(
                            ot.getId(),
                            ot.getNumero(),
                            new OrdenTransporteListResponse.PedidoResumenDTO(
                                    ot.getPedido() != null
                                            ? ot.getPedido().getId()
                                            : null,
                                    pedidoCodigo
                            ),
                            destinoStr,
                            ot.getTotalBultos(),
                            ot.getPesoTotal(),
                            ot.getEstado()
                    )
            );
        }

        return respuesta;
    }

    // =========================================================
    // OBTENER ORDEN POR ID
    // =========================================================

    @Transactional(readOnly = true)
    public OrdenTransporteDetailResponse obtenerPorId(
            String id,
            String empresaId) {

        OrdenTransporte ot =
                ordenTransporteRepository
                        .findByIdWithDetailsAndEmpresaId(
                                id,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Orden de transporte no encontrada"
                                )
                        );

        return mapToDetailResponse(ot);
    }

    // =========================================================
    // GENERAR ÓRDENES
    // =========================================================

    @Transactional
    public List<OrdenTransporteDetailResponse> generarOrdenes(
            String distribucionId,
            GenerarOrdenesRequest request,
            String empresaId) {

        Distribucion distribucion =
                distribucionRepository
                        .findByIdAndEmpresaId(
                                distribucionId,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Distribución no encontrada"
                                )
                        );

        if (!"CONFIRMADA".equalsIgnoreCase(
                String.valueOf(distribucion.getEstado()))) {

            throw new RuntimeException(
                    "La distribución debe estar en estado CONFIRMADA"
            );
        }

        if (ordenTransporteRepository
                .existsByDistribucionIdAndEmpresaId(
                        distribucionId,
                        empresaId
                )) {

            throw new DistribucionInvalidaException(
                    "Las órdenes de transporte de esta distribución ya fueron generadas"
            );
        }

        LocalTime vInicio =
                request != null
                        && request.ventanaInicio() != null
                        && !request.ventanaInicio().isBlank()
                        ? LocalTime.parse(request.ventanaInicio())
                        : null;

        LocalTime vFin =
                request != null
                        && request.ventanaFin() != null
                        && !request.ventanaFin().isBlank()
                        ? LocalTime.parse(request.ventanaFin())
                        : null;

        List<OrdenTransporte> ordenesCreadas =
                new ArrayList<>();

        List<Bulto> todosLosBultos =
                bultoRepository.findAll();

        for (var dp : distribucion.getPedidos()) {

            Pedido pedido = dp.getPedido();

            // El almacén de origen viene directamente
            // del pedido seleccionado al momento de crearlo.
            Almacen origenAlmacen =
                    pedido.getAlmacen();

            List<Bulto> bultosDelPedido =
                    new ArrayList<>();

            for (Bulto b : todosLosBultos) {

                if (b.getPedido() != null
                        && b.getPedido()
                                .getId()
                                .equals(pedido.getId())) {

                    bultosDelPedido.add(b);
                }
            }

            int totalBultos =
                    bultosDelPedido.size();

            BigDecimal pesoTotal =
                    BigDecimal.ZERO;

            for (Bulto b : bultosDelPedido) {

                if (b.getPeso() != null) {

                    pesoTotal =
                            pesoTotal.add(
                                    b.getPeso()
                            );
                }
            }

            String numeroOT =
                    "OT-"
                            + UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase();

            OrdenTransporte ot =
                    OrdenTransporte.builder()
                            .numero(numeroOT)
                            .distribucion(distribucion)
                            .pedido(pedido)
                            .origenAlmacen(origenAlmacen)
                            .destinoPuntoEntrega(
                                    pedido.getPuntoEntrega()
                            )
                            .totalBultos(totalBultos)
                            .pesoTotal(pesoTotal)
                            .ventanaInicio(vInicio)
                            .ventanaFin(vFin)
                            .estado("EMITIDA")
                            .build();

            ordenesCreadas.add(
                    ordenTransporteRepository.save(ot)
            );

            pedido.setEstado(
                    Pedido.Estado.LISTO_PARA_DESPACHO
            );

            pedidoRepository.save(pedido);
        }

        List<OrdenTransporteDetailResponse> respuesta =
                new ArrayList<>();

        for (OrdenTransporte ot : ordenesCreadas) {

            respuesta.add(
                    mapToDetailResponse(ot)
            );
        }

        return respuesta;
    }

    // =========================================================
    // ANULAR ORDEN
    // =========================================================

    @Transactional
    public OrdenTransporteDetailResponse anularOrden(
            String id,
            String empresaId) {

        OrdenTransporte ot =
                ordenTransporteRepository
                        .findByIdWithDetailsAndEmpresaId(
                                id,
                                empresaId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Orden de transporte no encontrada"
                                )
                        );

        if ("ANULADA".equalsIgnoreCase(
                ot.getEstado())) {

            throw new RuntimeException(
                    "La orden de transporte ya está anulada"
            );
        }

        ot.setEstado("ANULADA");

        ordenTransporteRepository.save(ot);

        Pedido pedido = ot.getPedido();

        pedido.setEstado(
                Pedido.Estado.PACKING_COMPLETADO
        );

        pedidoRepository.save(pedido);

        return mapToDetailResponse(ot);
    }

    // =========================================================
    // MAPEAR RESPUESTA
    // =========================================================

    private OrdenTransporteDetailResponse mapToDetailResponse(
            OrdenTransporte ot) {

        String transportistaNombre = null;

        if (ot.getDistribucion() != null
                && ot.getDistribucion()
                        .getTransportista() != null) {

            transportistaNombre =
                    ot.getDistribucion()
                            .getTransportista()
                            .getRazonSocial();
        }

        String pedidoId = null;
        String pedidoCodigo = null;

        if (ot.getPedido() != null) {

            pedidoId =
                    ot.getPedido().getId();

            pedidoCodigo =
                    ot.getPedido().getCodigo();
        }

        OrdenTransporteDetailResponse.DestinoDTO destino =
                null;

        if (ot.getPedido() != null
                && ot.getPedido().getPuntoEntrega() != null) {

            destino =
                    new OrdenTransporteDetailResponse.DestinoDTO(
                            ot.getPedido()
                                    .getPuntoEntrega()
                                    .getDireccion(),

                            ot.getPedido()
                                    .getPuntoEntrega()
                                    .getDistrito()
                    );
        }

        return new OrdenTransporteDetailResponse(

                ot.getId(),

                ot.getNumero(),

                new OrdenTransporteDetailResponse.PedidoResumenDTO(
                        pedidoId,
                        pedidoCodigo
                ),

                ot.getOrigenAlmacen() != null
                        ? new OrdenTransporteDetailResponse.AlmacenResumenDTO(
                                ot.getOrigenAlmacen().getId(),
                                ot.getOrigenAlmacen().getNombre()
                        )
                        : null,

                destino,

                transportistaNombre,

                ot.getTotalBultos(),

                ot.getPesoTotal(),

                ot.getVentanaInicio() != null
                        ? ot.getVentanaInicio().toString()
                        : null,

                ot.getVentanaFin() != null
                        ? ot.getVentanaFin().toString()
                        : null,

                ot.getEstado()
        );
    }
}