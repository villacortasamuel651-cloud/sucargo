import api from './api';

export async function listarDistribuciones(filtros = {}) {
  const { data } = await api.get('/distribuciones', { params: filtros });
  return data;
}

export async function obtenerDistribucion(id) {
  const { data } = await api.get(`/distribuciones/${id}`);
  return data;
}

export async function crearDistribucion(payload) {
  // payload = { transportistaId, fecha }
  const { data } = await api.post('/distribuciones', payload);
  return data;
}

export async function agregarPedido(distribucionId, pedidoId) {
  const { data } = await api.post(`/distribuciones/${distribucionId}/pedidos`, { pedidoId });
  return data;
}

export async function quitarPedido(distribucionId, pedidoId) {
  await api.delete(`/distribuciones/${distribucionId}/pedidos/${pedidoId}`);
}

export async function reordenarPedido(distribucionId, pedidoId, secuencia) {
  const { data } = await api.put(`/distribuciones/${distribucionId}/pedidos/${pedidoId}/secuencia`, { secuencia });
  return data;
}

export async function confirmarDistribucion(id) {
  const { data } = await api.post(`/distribuciones/${id}/confirmar`);
  return data;
}

export async function eliminarDistribucion(id) {
  await api.delete(`/distribuciones/${id}`);
}

export async function generarOrdenesTransporte(distribucionId, payload = {}) {
  // payload opcional = { ventanaInicio, ventanaFin }
  const { data } = await api.post(`/distribuciones/${distribucionId}/generar-ordenes`, payload);
  return data;
}