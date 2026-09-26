import api from './api';

export async function listarTransportistas(filtros = {}) {
  const { data } = await api.get('/transportistas', { params: filtros });
  return data;
}

export async function crearTransportista(payload) {
  const { data } = await api.post('/transportistas', payload);
  return data;
}

export async function editarTransportista(id, payload) {
  const { data } = await api.put(`/transportistas/${id}`, payload);
  return data;
}

export async function desactivarTransportista(id) {
  await api.delete(`/transportistas/${id}`);
}

export async function activarTransportista(id) {
  const { data } = await api.put(`/transportistas/${id}/estado`, { estado: 'ACTIVO' });
  return data;
}