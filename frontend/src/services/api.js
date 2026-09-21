import axios from 'axios';

// URL del backend. En desarrollo local apunta a Spring Boot corriendo en 8080.
const API_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
});

// Antes de cada request, si hay un accessToken guardado, lo agrega
// automáticamente al header Authorization (lo va a necesitar todo
// endpoint excepto login/register/refresh).
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// --- Funciones del módulo de autenticación (según contrato_api_acceso.md) ---

export async function login(email, password) {
  const { data } = await api.post('/auth/login', { email, password });
  return data;
}

export async function logout(refreshToken) {
  await api.post('/auth/logout', { refreshToken });
}

export async function getUsuarioActual() {
  const { data } = await api.get('/auth/me');
  return data;
}

export default api;
