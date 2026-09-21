package com.sucargo.backend.transportista.service;

import com.sucargo.backend.transportista.dto.CambiarEstadoRequest;
import com.sucargo.backend.transportista.dto.TransportistaRequest;
import com.sucargo.backend.transportista.dto.TransportistaResponse;
import com.sucargo.backend.transportista.entity.Transportista;
import com.sucargo.backend.transportista.repository.TransportistaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransportistaService {

    private final TransportistaRepository transportistaRepository;

    public List<TransportistaResponse> listar(String empresaId, String estadoFiltro) {
        Transportista.Estado estado = (estadoFiltro != null && !estadoFiltro.isBlank())
                ? Transportista.Estado.valueOf(estadoFiltro.toUpperCase())
                : null;

        return transportistaRepository.buscar(empresaId, estado).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TransportistaResponse crear(TransportistaRequest request, String empresaId) {
        Transportista transportista = Transportista.builder()
                .empresaId(empresaId)
                .razonSocial(request.razonSocial())
                .ruc(request.ruc())
                .contacto(request.contacto())
                .telefono(request.telefono())
                .correo(request.correo())
                .tipoServicio(request.tipoServicio())
                .estado(Transportista.Estado.ACTIVO)
                .build();

        return toResponse(transportistaRepository.save(transportista));
    }

    @Transactional
    public TransportistaResponse actualizar(String id, TransportistaRequest request, String empresaId) {
        Transportista transportista = obtenerPorIdYEmpresa(id, empresaId);

        transportista.setRazonSocial(request.razonSocial());
        transportista.setRuc(request.ruc());
        transportista.setContacto(request.contacto());
        transportista.setTelefono(request.telefono());
        transportista.setCorreo(request.correo());
        transportista.setTipoServicio(request.tipoServicio());

        return toResponse(transportistaRepository.save(transportista));
    }

    @Transactional
    public TransportistaResponse desactivar(String id, String empresaId) {
        Transportista transportista = obtenerPorIdYEmpresa(id, empresaId);
        transportista.setEstado(Transportista.Estado.INACTIVO);
        return toResponse(transportistaRepository.save(transportista));
    }

    @Transactional
    public TransportistaResponse cambiarEstado(String id, CambiarEstadoRequest request, String empresaId) {
        Transportista transportista = obtenerPorIdYEmpresa(id, empresaId);
        Transportista.Estado nuevoEstado = Transportista.Estado.valueOf(request.estado().toUpperCase());
        transportista.setEstado(nuevoEstado);
        return toResponse(transportistaRepository.save(transportista));
    }

    private Transportista obtenerPorIdYEmpresa(String id, String empresaId) {
        return transportistaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Transportista no encontrado"));
    }

    private TransportistaResponse toResponse(Transportista t) {
        return new TransportistaResponse(
                t.getId(),
                t.getRazonSocial(),
                t.getRuc(),
                t.getContacto(),
                t.getTelefono(),
                t.getCorreo(),
                t.getTipoServicio(),
                t.getEstado().name()
        );
    }
}