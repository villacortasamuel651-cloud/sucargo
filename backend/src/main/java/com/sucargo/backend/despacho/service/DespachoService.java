package com.sucargo.backend.despacho.service;

import com.sucargo.backend.despacho.dto.*;
import com.sucargo.backend.despacho.entity.Despacho;
import com.sucargo.backend.despacho.entity.DespachoBultoVerificado;
import com.sucargo.backend.despacho.repository.DespachoRepository;
import com.sucargo.backend.inventario.service.InventarioService;
import com.sucargo.backend.ordentransporte.entity.OrdenTransporte;
import com.sucargo.backend.ordentransporte.repository.OrdenTransporteRepository;
import com.sucargo.backend.packing.entity.Bulto;
import com.sucargo.backend.packing.repository.BultoRepository;
import com.sucargo.backend.pedido.entity.Pedido;
import com.sucargo.backend.pedido.entity.PedidoDetalle;
import com.sucargo.backend.pedido.repository.PedidoRepository;
import com.sucargo.backend.picking.entity.Picking;
import com.sucargo.backend.picking.entity.PickingDetalle;
import com.sucargo.backend.picking.repository.PickingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DespachoService {

    private final DespachoRepository despachoRepository;
    private final PedidoRepository pedidoRepository;
    private final BultoRepository bultoRepository;
    private final OrdenTransporteRepository ordenTransporteRepository;
    private final PickingRepository pickingRepository;
    private final InventarioService inventarioService;

    public List<DespachoListResponse> listar(
            String empresaId,
            String estadoFiltro) {

        Despacho.Estado estado =
                (estadoFiltro != null && !estadoFiltro.isBlank())
                        ? Despacho.Estado.valueOf(estadoFiltro)
                        : null;

        return despachoRepository.buscar(empresaId, estado)
                .stream()
                .map(this::toListDTO)
                .toList();
    }

    public DespachoDetailResponse obtener(
            String id,
            String empresaId) {

        Despacho despacho =
                buscarDespachoDeEmpresa(id, empresaId);

        return toDetailDTO(despacho, empresaId);
    }

    @Transactional
    public DespachoDetailResponse iniciar(
            IniciarDespachoRequest request,
            String empresaId) {

        /*
         * 1. Buscar pedido asegurando que pertenece
         *    a la empresa del usuario.
         */
        Pedido pedido =
                pedidoRepository.findByIdAndEmpresaId(
                        request.pedidoId(),
                        empresaId
                ).orElseThrow(() ->
                        new DespachoInvalidoException(
                                "El pedido no existe o no pertenece a la empresa"
                        )
                );

        /*
         * 2. El pedido debe estar listo para despacho.
         */
        if (pedido.getEstado() != Pedido.Estado.LISTO_PARA_DESPACHO) {
            throw new DespachoInvalidoException(
                    "El pedido debe estar en LISTO_PARA_DESPACHO para iniciar el despacho"
            );
        }

        /*
         * 3. Buscar la Orden de Transporte.
         *
         * La búsqueda debe respetar la empresa.
         */
        OrdenTransporte ot =
                ordenTransporteRepository
                        .findByPedidoIdAndEmpresaId(
                                pedido.getId(),
                                empresaId
                        )
                        .orElseThrow(() ->
                                new DespachoInvalidoException(
                                        "El pedido no tiene una Orden de Transporte generada"
                                )
                        );

        /*
         * 4. La OT debe estar emitida.
         */
        if (!"EMITIDA".equals(ot.getEstado())) {
            throw new DespachoInvalidoException(
                    "La Orden de Transporte de este pedido no está EMITIDA"
            );
        }

        /*
         * 5. No permitir dos despachos para el mismo pedido.
         */
        if (despachoRepository.existsByPedidoIdAndEmpresaId(
                pedido.getId(),
                empresaId)) {

            throw new DespachoInvalidoException(
                    "Ya existe un despacho para este pedido"
            );
        }

        /*
         * 6. Crear despacho.
         */
        Despacho nuevoDespacho =
                Despacho.builder()
                        .pedido(pedido)
                        .estado(Despacho.Estado.EN_VERIFICACION)
                        .build();

        Despacho despachoGuardado =
                despachoRepository.save(nuevoDespacho);

        /*
         * 7. Obtener los bultos del pedido.
         *
         * Se filtran por empresa.
         */
        List<Bulto> bultosDelPedido =
                bultoRepository
                        .findByPedidoIdAndEmpresaIdOrderByNumeroAsc(
                                pedido.getId(),
                                empresaId
                        );

        if (bultosDelPedido.isEmpty()) {
            throw new DespachoInvalidoException(
                    "El pedido no tiene bultos registrados"
            );
        }

        /*
         * 8. Crear las líneas de verificación.
         */
        List<DespachoBultoVerificado> lineas =
                bultosDelPedido.stream()
                        .map(b -> DespachoBultoVerificado.builder()
                                .despacho(despachoGuardado)
                                .bulto(b)
                                .verificado(false)
                                .build())
                        .toList();

        despachoGuardado.setBultos(
                new ArrayList<>(lineas)
        );

        Despacho despachoFinal =
                despachoRepository.save(despachoGuardado);

        return toDetailDTO(
                despachoFinal,
                empresaId
        );
    }

    @Transactional
    public DespachoDetailResponse verificarBulto(
            String despachoId,
            VerificarBultoRequest request,
            String empresaId) {

        Despacho despacho =
                buscarDespachoDeEmpresa(
                        despachoId,
                        empresaId
                );

        DespachoBultoVerificado linea =
                despacho.getBultos()
                        .stream()
                        .filter(b ->
                                b.getBulto()
                                        .getId()
                                        .equals(request.bultoId())
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new DespachoInvalidoException(
                                        "El bulto no pertenece al pedido de este despacho"
                                )
                        );

        if (Boolean.TRUE.equals(linea.getVerificado())) {
            throw new DespachoInvalidoException(
                    "Este bulto ya fue verificado"
            );
        }

        linea.setVerificado(true);
        linea.setFechaVerificacion(
                LocalDateTime.now()
        );

        despachoRepository.save(despacho);

        return toDetailDTO(
                despacho,
                empresaId
        );
    }

    @Transactional
    public CompletarDespachoResponse completar(
            String despachoId,
            String usuarioId,
            String empresaId) {

        Despacho despacho =
                buscarDespachoDeEmpresa(
                        despachoId,
                        empresaId
                );

        /*
         * Todos los bultos deben estar verificados.
         */
        boolean faltaAlguno =
                despacho.getBultos()
                        .stream()
                        .anyMatch(b ->
                                !Boolean.TRUE.equals(
                                        b.getVerificado()
                                )
                        );

        if (faltaAlguno) {
            throw new DespachoInvalidoException(
                    "Faltan bultos por verificar"
            );
        }

        Pedido pedido = despacho.getPedido();

        /*
         * El pedido tiene un único almacén de origen.
         *
         * Esto reemplaza completamente la antigua lógica
         * basada en ubicaciones.
         */
        if (pedido.getAlmacen() == null) {
            throw new DespachoInvalidoException(
                    "El pedido no tiene un almacén asignado"
            );
        }

        String almacenId =
                pedido.getAlmacen().getId();

        /*
         * Obtener picking del pedido respetando la empresa.
         */
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
                                new DespachoInvalidoException(
                                        "No se encontró el picking de este pedido"
                                )
                        );

        /*
         * Por cada línea del picking:
         *
         * 1. Liberamos la reserva original.
         * 2. Registramos la salida de lo realmente recolectado.
         *
         * Todo se realiza sobre el almacén del pedido.
         */
        for (PickingDetalle lineaPicking :
                picking.getLineas()) {

            PedidoDetalle detallePedido =
                    pedido.getItems()
                            .stream()
                            .filter(d ->
                                    d.getProducto()
                                            .getId()
                                            .equals(
                                                    lineaPicking
                                                            .getPedidoDetalle()
                                                            .getProducto()
                                                            .getId()
                                            )
                            )
                            .findFirst()
                            .orElseThrow(() ->
                                    new DespachoInvalidoException(
                                            "No se encontró la línea de pedido para "
                                                    + lineaPicking
                                                            .getPedidoDetalle()
                                                            .getProducto()
                                                            .getNombre()
                            )
                    );

            int cantidadReservada =
                    detallePedido.getCantidadReservada() != null
                            ? detallePedido.getCantidadReservada()
                            : 0;

            int cantidadRecolectada =
                    lineaPicking.getCantidadRecolectada() != null
                            ? lineaPicking.getCantidadRecolectada()
                            : 0;

            /*
             * Liberar la reserva.
             */
            if (cantidadReservada > 0) {

                inventarioService.registrarMovimientoInterno(
                        lineaPicking
                                .getPedidoDetalle()
                                .getProducto()
                                .getId(),

                        almacenId,

                        "LIBERACION",

                        cantidadReservada,

                        usuarioId,

                        "Liberación de reserva al despachar pedido "
                                + pedido.getCodigo()
                );
            }

            /*
             * Registrar la salida real.
             */
            if (cantidadRecolectada > 0) {

                inventarioService.registrarMovimientoInterno(
                        lineaPicking
                                .getPedidoDetalle()
                                .getProducto()
                                .getId(),

                        almacenId,

                        "SALIDA",

                        cantidadRecolectada,

                        usuarioId,

                        "Salida por despacho de pedido "
                                + pedido.getCodigo()
                );
            }
        }

        /*
         * Marcar despacho como despachado.
         */
        despacho.setEstado(
                Despacho.Estado.DESPACHADO
        );

        despacho.setFechaDespacho(
                LocalDateTime.now()
        );

        despachoRepository.save(despacho);

        /*
         * Avanzar el pedido.
         */
        pedido.setEstado(
                Pedido.Estado.DESPACHADO
        );

        pedidoRepository.save(pedido);

        return new CompletarDespachoResponse(
                despacho.getId(),
                despacho.getEstado().name(),
                pedido.getEstado().name()
        );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Despacho buscarDespachoDeEmpresa(
            String id,
            String empresaId) {

        return despachoRepository
                .findByIdAndEmpresaId(
                        id,
                        empresaId
                )
                .orElseThrow(
                        DespachoNoEncontradoException::new
                );
    }

    private String obtenerPickingIdDelPedido(
            String pedidoId,
            String empresaId) {

        return pickingRepository
                .findPickingIdByPedidoIdAndEmpresaId(
                        pedidoId,
                        empresaId
                )
                .orElseThrow(() ->
                        new DespachoInvalidoException(
                                "No se encontró el picking de este pedido"
                        )
                );
    }

    private DespachoListResponse toListDTO(
            Despacho despacho) {

        long verificados =
                despacho.getBultos()
                        .stream()
                        .filter(b ->
                                Boolean.TRUE.equals(
                                        b.getVerificado()
                                )
                        )
                        .count();

        return new DespachoListResponse(
                despacho.getId(),

                new PedidoResumenDTO(
                        despacho.getPedido().getId(),
                        despacho.getPedido().getCodigo()
                ),

                despacho.getEstado().name(),

                (int) verificados,

                despacho.getBultos().size()
        );
    }

    private DespachoDetailResponse toDetailDTO(
            Despacho despacho,
            String empresaId) {

        String numeroOT =
                ordenTransporteRepository
                        .findByPedidoIdAndEmpresaId(
                                despacho
                                        .getPedido()
                                        .getId(),
                                empresaId
                        )
                        .map(OrdenTransporte::getNumero)
                        .orElse(null);

        List<BultoVerificadoDTO> bultos =
                despacho.getBultos()
                        .stream()
                        .map(b ->
                                new BultoVerificadoDTO(
                                        b.getBulto().getId(),
                                        b.getBulto().getCodigo(),
                                        b.getBulto().getNumero(),
                                        b.getVerificado()
                                )
                        )
                        .toList();

        return new DespachoDetailResponse(
                despacho.getId(),

                new PedidoResumenDTO(
                        despacho.getPedido().getId(),
                        despacho.getPedido().getCodigo()
                ),

                numeroOT,

                despacho.getEstado().name(),

                bultos
        );
    }
}