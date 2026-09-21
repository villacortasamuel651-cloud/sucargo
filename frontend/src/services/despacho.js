import api from './api';

export async function listarDespachos(filtros = {}) {
  const { data } = await api.get('/despachos', { params: filtros });
  return data;
}

export async function obtenerDespacho(id) {
  const { data } = await api.get(`/despachos/${id}`);
  return data;
}

export async function iniciarDespacho(pedidoId) {
  const { data } = await api.post('/despachos', { pedidoId });
  return data;
}

export async function verificarBulto(despachoId, bultoId) {
  const { data } = await api.post(`/despachos/${despachoId}/verificar-bulto`, { bultoId });
  return data;
}

export async function completarDespacho(despachoId) {
  const { data } = await api.post(`/despachos/${despachoId}/completar`);
  return data;
}