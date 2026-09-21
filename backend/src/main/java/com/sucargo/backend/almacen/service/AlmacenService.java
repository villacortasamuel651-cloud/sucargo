package com.sucargo.backend.almacen.service;

import com.sucargo.backend.almacen.dto.AlmacenRequest;
import com.sucargo.backend.almacen.dto.AlmacenResponse;
import com.sucargo.backend.almacen.entity.Almacen;
import com.sucargo.backend.almacen.exception.AlmacenNotFoundException;
import com.sucargo.backend.almacen.repository.AlmacenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlmacenService {

    private final AlmacenRepository almacenRepository;

    @Transactional(readOnly = true)
    public List<AlmacenResponse> listarAlmacenes(String empresaId) {
        return almacenRepository
                .findByEmpresaIdAndEstado(
                        empresaId,
                        Almacen.Estado.ACTIVO
                )
                .stream()
                .map(alm -> AlmacenResponse.paraLista(
                        alm.getId(),
                        alm.getNombre(),
                        alm.getDireccion(),
                        alm.getEstado().name()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public AlmacenResponse obtenerPorId(String id, String empresaId) {
        Almacen almacen = buscarAlmacenOThrow(id, empresaId);
        return mapToResponse(almacen);
    }

    @Transactional
    public AlmacenResponse crear(
            AlmacenRequest request,
            String empresaId) {

        Almacen almacen = Almacen.builder()
                .empresaId(empresaId)
                .nombre(request.nombre())
                .direccion(request.direccion())
                .estado(Almacen.Estado.ACTIVO)
                .build();

        Almacen guardado = almacenRepository.save(almacen);

        return mapToResponse(guardado);
    }

    @Transactional
    public AlmacenResponse actualizar(
            String id,
            AlmacenRequest request,
            String empresaId) {

        Almacen almacen = buscarAlmacenOThrow(id, empresaId);

        almacen.setNombre(request.nombre());
        almacen.setDireccion(request.direccion());

        Almacen actualizado = almacenRepository.save(almacen);

        return mapToResponse(actualizado);
    }

    @Transactional
    public void desactivar(
            String id,
            String empresaId) {

        Almacen almacen = buscarAlmacenOThrow(id, empresaId);

        // TODO:
        // Validar posteriormente que el almacén no tenga
        // inventario activo antes de desactivarlo.

        almacen.setEstado(Almacen.Estado.INACTIVO);

        almacenRepository.save(almacen);
    }

    public Almacen buscarAlmacenOThrow(
            String id,
            String empresaId) {

        return almacenRepository
                .findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() ->
                        new AlmacenNotFoundException(
                                "Almacén no encontrado o no pertenece a la empresa."
                        ));
    }

    private AlmacenResponse mapToResponse(Almacen almacen) {

        return AlmacenResponse.paraDetalle(
                almacen.getId(),
                almacen.getNombre(),
                almacen.getDireccion(),
                almacen.getEstado().name()
        );
    }
}