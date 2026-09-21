package com.sucargo.backend.distribucion.service;

import com.sucargo.backend.distribucion.dto.*;
import com.sucargo.backend.distribucion.entity.Distribucion;
import com.sucargo.backend.distribucion.entity.DistribucionPedido;
import com.sucargo.backend.distribucion.repository.DistribucionPedidoRepository;
import com.sucargo.backend.distribucion.repository.DistribucionRepository;
import com.sucargo.backend.pedido.entity.Pedido;
import com.sucargo.backend.pedido.repository.PedidoRepository;
import com.sucargo.backend.transportista.entity.Transportista;
import com.sucargo.backend.transportista.repository.TransportistaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DistribucionService {

    private final DistribucionRepository distribucionRepository;
    private final DistribucionPedidoRepository distribucionPedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final TransportistaRepository transportistaRepository;

    public List<DistribucionListResponse> listar(
            String empresaId,
            String estadoFiltro,
            LocalDate fecha) {

        Distribucion.Estado estado = (estadoFiltro != null && !estadoFiltro.isBlank())
                ? Distribucion.Estado.valueOf(estadoFiltro.toUpperCase())
                : null;

        return distribucionRepository.buscar(empresaId, estado, fecha)
                .stream()
                .map(this::toListResponse)
                .toList();
    }

    public DistribucionDetailResponse obtener(String id, String empresaId) {
        Distribucion distribucion = obtenerPorIdYEmpresa(id, empresaId);
        return toDetailResponse(distribucion);
    }

    @Transactional
    public DistribucionDetailResponse crear(
            CrearDistribucionRequest request,
            String empresaId) {

        if (request.transportistaId() == null || request.transportistaId().isBlank()) {
            throw new DistribucionInvalidaException(
                    "El transportista es obligatorio");
        }

        if (request.fecha() == null) {
            throw new DistribucionInvalidaException(
                    "La fecha de distribución es obligatoria");
        }

        if (request.fecha().isBefore(LocalDate.now())) {
            throw new DistribucionInvalidaException(
                    "La fecha de distribución no puede ser anterior a hoy");
        }

        Transportista transportista = transportistaRepository
                .findByIdAndEmpresaId(request.transportistaId(), empresaId)
                .orElseThrow(() -> new DistribucionInvalidaException(
                        "El transportista no existe o no pertenece a la empresa"));

        if (!"ACTIVO".equalsIgnoreCase(transportista.getEstado().name())) {
            throw new DistribucionInvalidaException(
                    "El transportista seleccionado no está activo");
        }

        String codigoGenerado = "PD-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        Distribucion distribucion = Distribucion.builder()
                .empresaId(empresaId)
                .codigo(codigoGenerado)
                .transportista(transportista)
                .fecha(request.fecha())
                .estado(Distribucion.Estado.ABIERTA)
                .build();

        return toDetailResponse(
                distribucionRepository.save(distribucion)
        );
    }

    @Transactional
    public DistribucionPedidoDTO agregarPedido(
            String id,
            AgregarPedidoRequest request,
            String empresaId) {

        Distribucion distribucion = obtenerPorIdYEmpresa(id, empresaId);

        if (distribucion.getEstado() != Distribucion.Estado.ABIERTA) {
            throw new DistribucionInvalidaException(
                    "La distribución debe estar ABIERTA para agregar pedidos");
        }

        if (request.pedidoId() == null || request.pedidoId().isBlank()) {
            throw new DistribucionInvalidaException(
                    "El pedido es obligatorio");
        }

        Pedido pedido = pedidoRepository
                .findByIdAndEmpresaId(request.pedidoId(), empresaId)
                .orElseThrow(() -> new DistribucionInvalidaException(
                        "El pedido no existe o no pertenece a la empresa"));

        if (pedido.getEstado() != Pedido.Estado.PACKING_COMPLETADO) {
            throw new DistribucionInvalidaException(
                    "El pedido debe estar en estado PACKING_COMPLETADO");
        }

        if (distribucionPedidoRepository
                .existsByPedidoIdInDistribucionActiva(pedido.getId())) {

            throw new DistribucionInvalidaException(
                    "El pedido ya está en otra distribución activa (ABIERTA o CONFIRMADA)");
        }

        int siguienteSecuencia = distribucion.getPedidos().size() + 1;

        DistribucionPedido dp = DistribucionPedido.builder()
                .distribucion(distribucion)
                .pedido(pedido)
                .secuencia(siguienteSecuencia)
                .build();

        distribucion.getPedidos().add(dp);
        distribucionRepository.save(distribucion);

        return toPedidoDTO(dp);
    }

    @Transactional
    public void quitarPedido(
        String id,
        String pedidoId,
        String empresaId) {

    Distribucion distribucion = obtenerPorIdYEmpresa(id, empresaId);

    if (distribucion.getEstado() != Distribucion.Estado.ABIERTA) {
        throw new DistribucionInvalidaException(
                "La distribución debe estar ABIERTA para quitar pedidos");
    }

    DistribucionPedido dp = distribucionPedidoRepository
            .findByDistribucionIdAndPedidoId(id, pedidoId)
            .orElseThrow(() -> new DistribucionInvalidaException(
                    "El pedido no pertenece a esta distribución"));

    int secuenciaEliminada = dp.getSecuencia();

    distribucion.getPedidos().remove(dp);

    // Cerrar el espacio dejado por el pedido eliminado
    distribucion.getPedidos()
            .stream()
            .filter(p -> p.getSecuencia() > secuenciaEliminada)
            .forEach(p -> p.setSecuencia(p.getSecuencia() - 1));

    distribucionRepository.save(distribucion);
    }

    @Transactional
public DistribucionPedidoDTO reordenarSecuencia(
        String id,
        String pedidoId,
        ReordenarSecuenciaRequest request,
        String empresaId) {

    Distribucion distribucion = obtenerPorIdYEmpresa(id, empresaId);

    if (distribucion.getEstado() != Distribucion.Estado.ABIERTA) {
        throw new DistribucionInvalidaException(
                "La distribución debe estar ABIERTA para cambiar secuencias");
    }

    DistribucionPedido actual = distribucion.getPedidos()
            .stream()
            .filter(p -> p.getPedido().getId().equals(pedidoId))
            .findFirst()
            .orElseThrow(() -> new DistribucionInvalidaException(
                    "El pedido no pertenece a esta distribución"));

    int secuenciaActual = actual.getSecuencia();
    int nuevaSecuencia = request.secuencia();

    if (nuevaSecuencia < 1 ||
            nuevaSecuencia > distribucion.getPedidos().size()) {

        throw new DistribucionInvalidaException(
                "La secuencia debe estar entre 1 y " +
                        distribucion.getPedidos().size());
    }

    if (secuenciaActual == nuevaSecuencia) {
        return toPedidoDTO(actual);
    }

    DistribucionPedido objetivo = distribucion.getPedidos()
            .stream()
            .filter(p -> p.getSecuencia() == nuevaSecuencia)
            .findFirst()
            .orElseThrow(() -> new DistribucionInvalidaException(
                    "No se encontró el pedido de la posición solicitada"));

    /*
     * La tabla tiene UNIQUE(distribucion_id, secuencia).
     *
     * Por eso primero liberamos la secuencia actual
     * usando temporalmente -1.
     */

    actual.setSecuencia(-1);
    distribucionPedidoRepository.saveAndFlush(actual);

    /*
     * Ahora podemos darle al pedido objetivo
     * la secuencia que tenía el pedido actual.
     */
    objetivo.setSecuencia(secuenciaActual);
    distribucionPedidoRepository.saveAndFlush(objetivo);

    /*
     * Finalmente el pedido movido toma su nueva posición.
     */
    actual.setSecuencia(nuevaSecuencia);
    distribucionPedidoRepository.saveAndFlush(actual);

    return toPedidoDTO(actual);
}

    @Transactional
    public DistribucionDetailResponse confirmar(
            String id,
            String empresaId) {

        Distribucion distribucion = obtenerPorIdYEmpresa(id, empresaId);

        if (distribucion.getEstado() != Distribucion.Estado.ABIERTA) {
            throw new DistribucionInvalidaException(
                    "La distribución debe estar ABIERTA para confirmarse");
        }

        if (distribucion.getPedidos().isEmpty()) {
            throw new DistribucionInvalidaException(
                    "La distribución debe contener al menos 1 pedido para confirmarse");
        }

        List<Integer> secuencias = distribucion.getPedidos()
                .stream()
                .map(DistribucionPedido::getSecuencia)
                .sorted()
                .toList();

        for (int i = 0; i < secuencias.size(); i++) {
            if (secuencias.get(i) != i + 1) {
                throw new DistribucionInvalidaException(
                        "Las secuencias de los pedidos deben ser consecutivas");
            }
        }

        for (DistribucionPedido dp : distribucion.getPedidos()) {

            if (dp.getPedido().getEstado() != Pedido.Estado.PACKING_COMPLETADO) {
                throw new DistribucionInvalidaException(
                        "El pedido " +
                                dp.getPedido().getCodigo() +
                                " ya no está en PACKING_COMPLETADO");
            }

            if (distribucionPedidoRepository
                    .existsByPedidoIdInDistribucionActiva(dp.getPedido().getId())) {

                // El propio pedido pertenece a esta distribución,
                // por lo que buscamos si existe otra distribución activa.
                long otrasDistribuciones = distribucion.getPedidos()
                        .stream()
                        .filter(otro -> otro.getPedido().getId()
                                .equals(dp.getPedido().getId()))
                        .count();

                if (otrasDistribuciones == 0) {
                    throw new DistribucionInvalidaException(
                            "El pedido " +
                                    dp.getPedido().getCodigo() +
                                    " ya pertenece a otra distribución activa");
                }
            }
        }

        distribucion.setEstado(Distribucion.Estado.CONFIRMADA);

        return toDetailResponse(
                distribucionRepository.save(distribucion)
        );
    }

    @Transactional
    public void eliminar(
        String id,
        String empresaId) {

    Distribucion distribucion = obtenerPorIdYEmpresa(id, empresaId);

    if (distribucion.getEstado() != Distribucion.Estado.ABIERTA) {
        throw new DistribucionInvalidaException(
                "Solo se pueden eliminar distribuciones en estado ABIERTA");
    }

    distribucionRepository.delete(distribucion);
    }

    private Distribucion obtenerPorIdYEmpresa(
            String id,
            String empresaId) {

        return distribucionRepository
                .findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new DistribucionInvalidaException(
                        "Distribución no encontrada"));
    }

    private DistribucionListResponse toListResponse(
            Distribucion d) {

        return new DistribucionListResponse(
                d.getId(),
                d.getCodigo(),
                new TransportistaResumenDTO(
                        d.getTransportista().getId(),
                        d.getTransportista().getRazonSocial()
                ),
                d.getFecha(),
                d.getEstado().name(),
                d.getPedidos().size()
        );
    }

    private DistribucionDetailResponse toDetailResponse(
            Distribucion d) {

        List<DistribucionPedidoDTO> pedidosDTO = d.getPedidos()
                .stream()
                .sorted(Comparator.comparingInt(
                        DistribucionPedido::getSecuencia))
                .map(this::toPedidoDTO)
                .toList();

        return new DistribucionDetailResponse(
                d.getId(),
                d.getCodigo(),
                new TransportistaResumenDTO(
                        d.getTransportista().getId(),
                        d.getTransportista().getRazonSocial()
                ),
                d.getFecha(),
                d.getEstado().name(),
                pedidosDTO
        );
    }

    private DistribucionPedidoDTO toPedidoDTO(
            DistribucionPedido dp) {

        String clienteNombre = dp.getPedido().getCliente() != null
                ? dp.getPedido().getCliente().getRazonSocial()
                : "Sin cliente";

        return new DistribucionPedidoDTO(
                dp.getPedido().getId(),
                dp.getPedido().getCodigo(),
                clienteNombre,
                dp.getSecuencia()
        );
    }
}