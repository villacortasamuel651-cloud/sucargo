import api from './api';

export async function listarUsuarios() {
  const { data } = await api.get('/usuarios');
  return data;
}

export async function obtenerUsuario(id) {
  const { data } = await api.get(`/usuarios/${id}`);
  return data;
}

export async function crearUsuario(payload) {
  const { data } = await api.post('/usuarios', payload);
  return data;
}

export async function editarPerfil(id, payload) {
  const { data } = await api.put(`/usuarios/${id}`, payload);
  return data;
}

export async function cambiarRol(id, rol) {
  const { data } = await api.put(`/usuarios/${id}/rol`, { rol });
  return data;
}

export async function cambiarEstado(id, estado) {
  const { data } = await api.put(`/usuarios/${id}/estado`, { estado });
  return data;
}

export async function cerrarSesionesRemotas(id) {
  await api.post(`/usuarios/${id}/cerrar-sesiones`);
}
