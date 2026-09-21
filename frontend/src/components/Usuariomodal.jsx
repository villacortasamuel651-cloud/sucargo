import { useState } from 'react';
import { crearUsuario } from '../services/usuarios';
import './ClienteModal.css';

const ROLES = [
  'ADMIN_EMPRESA', 'VENTAS', 'ALMACENERO',
  'SUPERVISOR_ALMACEN', 'LOGISTICA', 'DESPACHO',
];

const VACIO = { nombre: '', email: '', password: '', rol: 'VENTAS' };

export default function UsuarioModal({ onGuardar, onCerrar }) {
  const [form, setForm] = useState(VACIO);
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (!form.nombre.trim() || !form.email.trim() || !form.password.trim()) {
      setError('Nombre, correo y contraseña son obligatorios.');
      return;
    }
    if (form.password.length < 6) {
      setError('La contraseña debe tener al menos 6 caracteres.');
      return;
    }

    setGuardando(true);
    try {
      const usuario = await crearUsuario(form);
      onGuardar(usuario);
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo crear el usuario (¿correo ya registrado?).');
    } finally {
      setGuardando(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <h2>Nuevo usuario</h2>

        <form onSubmit={handleSubmit} className="modal-form">
          <label>Nombre completo</label>
          <input name="nombre" value={form.nombre} onChange={handleChange} placeholder="Silvia Ramos" />

          <label>Correo electrónico</label>
          <input type="email" name="email" value={form.email} onChange={handleChange} placeholder="silvia.ramos@elbuenprecio.pe" />

          <label>Contraseña temporal</label>
          <input type="password" name="password" value={form.password} onChange={handleChange} placeholder="Mínimo 6 caracteres" />

          <label>Rol</label>
          <select name="rol" value={form.rol} onChange={handleChange}>
            {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
          </select>

          {error && <p className="modal-error">{error}</p>}

          <div className="modal-acciones">
            <button type="button" className="modal-cancelar" onClick={onCerrar}>Cancelar</button>
            <button type="submit" disabled={guardando}>{guardando ? 'Creando…' : 'Crear usuario'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}