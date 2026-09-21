import api from './api';

export async function listarCategorias() {
  const { data } = await api.get('/categorias-producto');
  return data;
}

export async function crearCategoria(nombre) {
  const { data } = await api.post('/categorias-producto', { nombre });
  return data;
}

export async function listarProductos() {
  const { data } = await api.get('/productos');
  return data;
}

export async function crearProducto(payload) {
  // payload = { sku, nombre, descripcion, categoriaId, unidadMedida, peso, stockMinimo }
  const { data } = await api.post('/productos', payload);
  return data;
}

export async function editarProducto(id, payload) {
  const { data } = await api.put(`/productos/${id}`, payload);
  return data;
}

export async function desactivarProducto(id) {
  await api.delete(`/productos/${id}`);
}
