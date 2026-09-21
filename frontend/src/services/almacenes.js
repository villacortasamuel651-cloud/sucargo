import api from './api';

export async function listarAlmacenes() {
  const { data } = await api.get('/almacenes');
  return data;
}

export async function obtenerAlmacen(id) {
  const { data } = await api.get(`/almacenes/${id}`);
  return data;
}

export async function crearAlmacen(payload) {
  const { data } = await api.post('/almacenes', payload);
  return data;
}

export async function editarAlmacen(id, payload) {
  const { data } = await api.put(`/almacenes/${id}`, payload);
  return data;
}

export async function desactivarAlmacen(id) {
  await api.delete(`/almacenes/${id}`);
}