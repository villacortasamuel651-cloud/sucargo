import api from './api';

export async function listarInventario(filtros = {}) {
  const { data } = await api.get('/inventario', {
    params: filtros,
  });

  return data;
}

export async function obtenerMovimientos(inventarioId) {
  const { data } = await api.get(
    `/inventario/${inventarioId}/movimientos`
  );

  return data;
}

export async function registrarMovimiento(payload) {
  // payload = {
  //   productoId,
  //   almacenId,
  //   tipo,
  //   cantidad,
  //   motivo
  // }

  const { data } = await api.post(
    '/inventario/movimientos',
    payload
  );

  return data;
}

