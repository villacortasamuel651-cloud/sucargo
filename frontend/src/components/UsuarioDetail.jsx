import { useState } from 'react';
import DetailTabs from './DetailTabs';
import {
  editarPerfil,
  cambiarRol,
  cambiarEstado,
  cerrarSesionesRemotas,
} from '../services/usuarios';
import './UsuarioDetail.css';

const ROLES = [
  'ADMIN_EMPRESA', 'VENTAS', 'ALMACENERO',
  'SUPERVISOR_ALMACEN', 'LOGISTICA', 'DESPACHO',
];

export default function UsuarioDetail({ usuario, onActualizar, onCerrar }) {
  const [nombre, setNombre] = useState(usuario.nombre);
  const [guardandoPerfil, setGuardandoPerfil] = useState(false);

  const [rolSeleccionado, setRolSeleccionado] = useState(usuario.rol);
  const [guardandoRol, setGuardandoRol] = useState(false);

  const [cambiandoEstado, setCambiandoEstado] = useState(false);
  const [cerrandoSesiones, setCerrandoSesiones] = useState(false);

  const inactivo = usuario.estado === 'INACTIVO';

  async function handleGuardarPerfil() {
    setGuardandoPerfil(true);
    try {
      const actualizado = await editarPerfil(usuario.id, { nombre });
      onActualizar(actualizado);
    } finally {
      setGuardandoPerfil(false);
    }
  }

  async function handleGuardarRol() {
    setGuardandoRol(true);
    try {
      const actualizado = await cambiarRol(usuario.id, rolSeleccionado);
      onActualizar({ ...usuario, ...actualizado });
    } finally {
      setGuardandoRol(false);
    }
  }

  async function handleToggleEstado() {
    const nuevoEstado = inactivo ? 'ACTIVO' : 'INACTIVO';
    if (!confirm(`¿${inactivo ? 'Activar' : 'Desactivar'} a ${usuario.nombre}?`)) return;

    setCambiandoEstado(true);
    try {
      const actualizado = await cambiarEstado(usuario.id, nuevoEstado);
      onActualizar({ ...usuario, ...actualizado });
    } finally {
      setCambiandoEstado(false);
    }
  }

  async function handleCerrarSesiones() {
    if (!confirm(`¿Cerrar todas las sesiones activas de ${usuario.nombre}?`)) return;
    setCerrandoSesiones(true);
    try {
      await cerrarSesionesRemotas(usuario.id);
      alert('Sesiones cerradas correctamente.');
    } finally {
      setCerrandoSesiones(false);
    }
  }

  const tabs = [
    {
      id: 'perfil',
      label: 'Perfil',
      content: (
        <div className="usuario-tab-section">
          <label>Nombre completo</label>
          <input value={nombre} onChange={(e) => setNombre(e.target.value)} />

          <label>Correo electrónico</label>
          <input value={usuario.email} disabled />

          <label>Registrado el</label>
          <p className="usuario-dato-readonly">
            {new Date(usuario.createdAt).toLocaleDateString('es-PE')}
          </p>

          <button className="usuario-btn-primario" onClick={handleGuardarPerfil} disabled={guardandoPerfil}>
            {guardandoPerfil ? 'Guardando…' : 'Guardar cambios'}
          </button>
        </div>
      ),
    },
    {
      id: 'rol',
      label: 'Rol y permisos',
      content: (
        <div className="usuario-tab-section">
          <label>Rol asignado</label>
          <select value={rolSeleccionado} onChange={(e) => setRolSeleccionado(e.target.value)}>
            {ROLES.map((r) => (
              <option key={r} value={r}>{r}</option>
            ))}
          </select>
          <p className="usuario-hint">
            El rol determina qué módulos puede ver este usuario en el sidebar.
          </p>

          <button className="usuario-btn-primario" onClick={handleGuardarRol} disabled={guardandoRol}>
            {guardandoRol ? 'Guardando…' : 'Actualizar rol'}
          </button>
        </div>
      ),
    },
    {
      id: 'seguridad',
      label: 'Seguridad',
      content: (
        <div className="usuario-tab-section">
          <label>Último inicio de sesión</label>
          <p className="usuario-dato-readonly">
            {usuario.ultimoLogin
              ? new Date(usuario.ultimoLogin).toLocaleString('es-PE')
              : 'Sin registro'}
          </p>

          <label>Sesiones activas</label>
          <p className="usuario-dato-readonly">{usuario.sesionesActivas ?? '—'}</p>

          <button
            className="usuario-btn-peligro"
            onClick={handleCerrarSesiones}
            disabled={cerrandoSesiones}
          >
            {cerrandoSesiones ? 'Cerrando…' : 'Cerrar todas las sesiones'}
          </button>
        </div>
      ),
    },
    {
      id: 'estado',
      label: 'Estado',
      content: (
        <div className="usuario-tab-section">
          <label>Estado actual</label>
          <span className={`usuario-badge ${inactivo ? 'usuario-badge-inactivo' : ''}`}>
            {usuario.estado}
          </span>

          <p className="usuario-hint" style={{ marginTop: '0.75rem' }}>
            {inactivo
              ? 'Este usuario no puede iniciar sesión mientras esté inactivo.'
              : 'Al desactivar, se cerrarán automáticamente todas sus sesiones.'}
          </p>

          <button
            className={inactivo ? 'usuario-btn-primario' : 'usuario-btn-peligro'}
            onClick={handleToggleEstado}
            disabled={cambiandoEstado}
          >
            {cambiandoEstado ? 'Procesando…' : inactivo ? 'Activar usuario' : 'Desactivar usuario'}
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="usuario-detail">
      <div className="usuario-detail-header">
        <div>
          <h2>{usuario.nombre}</h2>
          <p className="usuario-detail-email">{usuario.email}</p>
        </div>
        <button className="usuario-detail-cerrar" onClick={onCerrar}>Cerrar</button>
      </div>

      <DetailTabs tabs={tabs} />
    </div>
  );
}
