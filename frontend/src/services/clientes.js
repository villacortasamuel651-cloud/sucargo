import api from './api';

export async function listarClientes() {
  const { data } = await api.get('/clientes');
  return data;
}

export async function obtenerCliente(id) {
  const { data } = await api.get(`/clientes/${id}`);
  return data;
}

export async function crearCliente(payload) {
  // payload = { razonSocial, rucDni, telefono }
  const { data } = await api.post('/clientes', payload);
  return data;
}

export async function editarCliente(id, payload) {
  const { data } = await api.put(`/clientes/${id}`, payload);
  return data;
}

export async function desactivarCliente(id) {
  await api.delete(`/clientes/${id}`);
}

export async function agregarPuntoEntrega(clienteId, payload) {
  // payload = { direccion, distrito, referencia }
  const { data } = await api.post(`/clientes/${clienteId}/puntos-entrega`, payload);
  return data;
}

export async function desactivarPuntoEntrega(clienteId, puntoId) {
  await api.delete(`/clientes/${clienteId}/puntos-entrega/${puntoId}`);
}
