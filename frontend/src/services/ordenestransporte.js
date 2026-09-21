import api from './api';

export async function listarOrdenes(filtros = {}) {
  const { data } = await api.get('/ordenes-transporte', { params: filtros });
  return data;
}

export async function obtenerOrden(id) {
  const { data } = await api.get(`/ordenes-transporte/${id}`);
  return data;
}

export async function anularOrden(id) {
  const { data } = await api.post(`/ordenes-transporte/${id}/anular`);
  return data;
}