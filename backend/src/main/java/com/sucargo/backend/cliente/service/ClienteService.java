package com.sucargo.backend.cliente.service;

import com.sucargo.backend.cliente.dto.*;
import com.sucargo.backend.cliente.entity.Cliente;
import com.sucargo.backend.cliente.entity.PuntoEntrega;
import com.sucargo.backend.cliente.repository.ClienteRepository;
import com.sucargo.backend.cliente.repository.PuntoEntregaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PuntoEntregaRepository puntoEntregaRepository;

    // --- Clientes ---

    public List<ClienteResponse> listar(String empresaId, String estadoFiltro, String textoFiltro) {
        List<Cliente> clientes;

        if (textoFiltro != null && !textoFiltro.isBlank()) {
            clientes = clienteRepository.buscarPorTexto(empresaId, textoFiltro);
        } else if (estadoFiltro != null && !estadoFiltro.isBlank()) {
            clientes = clienteRepository.findByEmpresaIdAndEstado(empresaId, Cliente.Estado.valueOf(estadoFiltro));
        } else {
            clientes = clienteRepository.findByEmpresaIdAndEstado(empresaId, Cliente.Estado.ACTIVO);
        }

        return clientes.stream()
                .map(c -> ClienteResponse.paraLista(
                        c.getId(), c.getRazonSocial(), c.getRucDni(), c.getTelefono(),
                        c.getEstado().name(), c.getPuntosEntrega().size()))
                .toList();
    }

    public ClienteResponse obtener(String id, String empresaId) {
        Cliente cliente = buscarClienteDeEmpresa(id, empresaId);
        return toDetalleDTO(cliente);
    }

    @SuppressWarnings("null")
    @Transactional
    public ClienteResponse crear(ClienteRequest request, String empresaId) {
        if (clienteRepository.existsByEmpresaIdAndRucDni(empresaId, request.rucDni())) {
            throw new RucDniYaExisteException();
        }

        Cliente cliente = Cliente.builder()
                .empresaId(empresaId)
                .razonSocial(request.razonSocial())
                .rucDni(request.rucDni())
                .telefono(request.telefono())
                .estado(Cliente.Estado.ACTIVO)
                .build();

        Cliente guardado = clienteRepository.save(cliente);
        return toDetalleDTO(guardado);
    }

    @Transactional
    public ClienteResponse editar(String id, ClienteRequest request, String empresaId) {
        Cliente cliente = buscarClienteDeEmpresa(id, empresaId);

        // Si cambió el RUC/DNI, hay que validar que el nuevo no choque con otro cliente
        if (!cliente.getRucDni().equals(request.rucDni())
                && clienteRepository.existsByEmpresaIdAndRucDni(empresaId, request.rucDni())) {
            throw new RucDniYaExisteException();
        }

        cliente.setRazonSocial(request.razonSocial());
        cliente.setRucDni(request.rucDni());
        cliente.setTelefono(request.telefono());

        Cliente actualizado = clienteRepository.save(cliente);
        return toDetalleDTO(actualizado);
    }

    @Transactional
    public void desactivar(String id, String empresaId) {
        Cliente cliente = buscarClienteDeEmpresa(id, empresaId);
        cliente.setEstado(Cliente.Estado.INACTIVO);
        clienteRepository.save(cliente);
    }

    // --- Puntos de entrega ---

    @SuppressWarnings("null")
    @Transactional
    public PuntoEntregaResponse agregarPuntoEntrega(
            String clienteId, PuntoEntregaRequest request, String empresaId) {

        Cliente cliente = buscarClienteDeEmpresa(clienteId, empresaId);

        PuntoEntrega punto = PuntoEntrega.builder()
                .cliente(cliente)
                .direccion(request.direccion())
                .distrito(request.distrito())
                .referencia(request.referencia())
                .estado(PuntoEntrega.Estado.ACTIVO)
                .build();

        PuntoEntrega guardado = puntoEntregaRepository.save(punto);
        return toPuntoEntregaDTO(guardado);
    }

    @Transactional
    public PuntoEntregaResponse editarPuntoEntrega(
            String clienteId, String puntoId, PuntoEntregaRequest request, String empresaId) {

        buscarClienteDeEmpresa(clienteId, empresaId); // valida que el cliente sea de la empresa
        PuntoEntrega punto = buscarPuntoDeCliente(puntoId, clienteId);

        punto.setDireccion(request.direccion());
        punto.setDistrito(request.distrito());
        punto.setReferencia(request.referencia());

        PuntoEntrega actualizado = puntoEntregaRepository.save(punto);
        return toPuntoEntregaDTO(actualizado);
    }

    @Transactional
    public void desactivarPuntoEntrega(String clienteId, String puntoId, String empresaId) {
        buscarClienteDeEmpresa(clienteId, empresaId);
        PuntoEntrega punto = buscarPuntoDeCliente(puntoId, clienteId);

        // TODO: validar pedidos pendientes cuando exista ese módulo.
        // Regla de negocio: un cliente debe conservar al menos un punto de
        // entrega activo si tiene pedidos pendientes. Por ahora no hay
        // módulo de pedidos, así que no se puede verificar esto todavía.

        punto.setEstado(PuntoEntrega.Estado.INACTIVO);
        puntoEntregaRepository.save(punto);
    }

    // --- Helpers privados ---

    private Cliente buscarClienteDeEmpresa(String id, String empresaId) {
        return clienteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(ClienteNoEncontradoException::new);
    }

    private PuntoEntrega buscarPuntoDeCliente(String puntoId, String clienteId) {
        return puntoEntregaRepository.findByIdAndClienteId(puntoId, clienteId)
                .orElseThrow(PuntoEntregaNoEncontradoException::new);
    }

    private ClienteResponse toDetalleDTO(Cliente cliente) {
        List<PuntoEntregaResponse> puntos = cliente.getPuntosEntrega().stream()
                .map(this::toPuntoEntregaDTO)
                .toList();

        return ClienteResponse.paraDetalle(
                cliente.getId(), cliente.getRazonSocial(), cliente.getRucDni(),
                cliente.getTelefono(), cliente.getEstado().name(), puntos);
    }

    private PuntoEntregaResponse toPuntoEntregaDTO(PuntoEntrega punto) {
        return new PuntoEntregaResponse(
                punto.getId(), punto.getDireccion(), punto.getDistrito(),
                punto.getReferencia(), punto.getEstado().name());
    }
}