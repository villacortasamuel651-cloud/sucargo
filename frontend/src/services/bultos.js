import api from './api';

export async function listarBultos(pedidoId) {
  const { data } = await api.get('/bultos', { params: { pedidoId } });
  return data;
}

export async function obtenerProductosDelPedido(pedidoId) {
  const { data } = await api.get(
    `/bultos/pedido/${pedidoId}/productos`
  );
  return data;
}

export async function obtenerBulto(id) {
  const { data } = await api.get(`/bultos/${id}`);
  return data;
}

export async function crearBulto(pedidoId) {
  const { data } = await api.post('/bultos', { pedidoId });
  return data;
}

export async function agregarProducto(bultoId, payload) {
  // payload = { productoId, cantidad }
  const { data } = await api.post(`/bultos/${bultoId}/productos`, payload);
  return data;
}

export async function quitarProducto(bultoId, detalleId) {
  await api.delete(`/bultos/${bultoId}/productos/${detalleId}`);
}

export async function registrarPeso(bultoId, peso) {
  const { data } = await api.put(`/bultos/${bultoId}/peso`, { peso });
  return data;
}

export async function cerrarBulto(bultoId) {
  const { data } = await api.post(`/bultos/${bultoId}/cerrar`);
  return data;
}

export async function completarPacking(pedidoId) {
  const { data } = await api.post(`/pedidos/${pedidoId}/completar-packing`);
  return data;
}

export async function pendienteEmpaquetar(pedidoId) {
  const { data } = await api.get(`/pedidos/${pedidoId}/pendiente-empaquetar`);
  return data;
}

export async function eliminarBulto(bultoId) {
  await api.delete(`/bultos/${bultoId}`);
}