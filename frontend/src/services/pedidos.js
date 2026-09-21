import api from './api';

export async function listarPedidos(filtros = {}) {
  const { data } = await api.get('/pedidos', { params: filtros });
  return data;
}

export async function obtenerPedido(id) {
  const { data } = await api.get(`/pedidos/${id}`);
  return data;
}

export async function obtenerAlmacenesDisponibles(items) {
  const { data } = await api.post(
    '/pedidos/almacenes-disponibles',
    { items }
  );

  return data;
}

export async function crearPedido(payload) {
  // payload = {
  //   clienteId,
  //   puntoEntregaId,
  //   almacenId,
  //   prioridad,
  //   items: [{ productoId, cantidad }]
  // }
  const { data } = await api.post('/pedidos', payload);
  return data;
}

export async function cancelarPedido(id) {
  const { data } = await api.post(`/pedidos/${id}/cancelar`);
  return data;
}

export async function listarPedidosDisponiblesParaDistribucion() {
  const { data } = await api.get('/pedidos/disponibles-distribucion');
  return data;
}