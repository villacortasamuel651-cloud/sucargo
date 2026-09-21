import api from './api';

export async function listarPicking(filtros = {}) {
  const { data } = await api.get('/picking', { params: filtros });
  return data;
}

export async function obtenerPicking(id) {
  const { data } = await api.get(`/picking/${id}`);
  return data;
}

export async function generarPicking(pedidoId) {
  const { data } = await api.post('/picking/generar', { pedidoId });
  return data;
}

export async function iniciarPicking(id) {
  const { data } = await api.post(`/picking/${id}/iniciar`);
  return data;
}

export async function registrarLinea(pickingId, lineaId, payload) {
  // payload = { cantidadRecolectada, incidencia }
  const { data } = await api.put(`/picking/${pickingId}/lineas/${lineaId}`, payload);
  return data;
}

export async function completarPicking(id) {
  const { data } = await api.post(`/picking/${id}/completar`);
  return data;
}