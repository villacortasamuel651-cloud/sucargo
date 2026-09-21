import { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import UsuarioDetail from '../components/UsuarioDetail';
import UsuarioModal from '../components/UsuarioModal';
import { useAuth } from '../context/AuthContext';
import { listarUsuarios } from '../services/usuarios';
import './Usuarios.css';

export default function Usuarios() {
  const { usuario: usuarioActual } = useAuth();
  const [usuarios, setUsuarios] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [seleccionado, setSeleccionado] = useState(null);
  const [modalNuevo, setModalNuevo] = useState(false);

  useEffect(() => {
    cargarUsuarios();
  }, []);

  async function cargarUsuarios() {
    setCargando(true);
    setError('');
    try {
      const data = await listarUsuarios();
      setUsuarios(data);
    } catch (err) {
      setError('No se pudo conectar con el servidor.');
    } finally {
      setCargando(false);
    }
  }

  function handleActualizar(usuarioActualizado) {
    setUsuarios((prev) =>
      prev.map((u) => (u.id === usuarioActualizado.id ? { ...u, ...usuarioActualizado } : u))
    );
    setSeleccionado((prev) => (prev ? { ...prev, ...usuarioActualizado } : prev));
  }

  function handleUsuarioCreado(nuevo) {
    setModalNuevo(false);
    cargarUsuarios();
  }

  return (
    <div className="dashboard-layout">
      <Sidebar rol={usuarioActual.rol} />

      <div className="dashboard-content">
        <header className="usuarios-header">
          <h2>Usuarios</h2>
          <button className="usuarios-nuevo-btn" onClick={() => setModalNuevo(true)}>+ Nuevo usuario</button>
        </header>

        <main className="usuarios-main">
          {cargando && <p>Cargando usuarios…</p>}
          {error && <p className="usuarios-error">{error}</p>}

          {!cargando && !error && (
            <div className="usuarios-layout">
              <div className="usuarios-lista">
                {usuarios.map((u) => (
                  <button
                    key={u.id}
                    className={`usuarios-item ${seleccionado?.id === u.id ? 'usuarios-item-activo' : ''}`}
                    onClick={() => setSeleccionado(u)}
                  >
                    <span className="usuarios-item-nombre">{u.nombre}</span>
                    <span className="usuarios-item-rol">{u.rol}</span>
                  </button>
                ))}
              </div>

              <div className="usuarios-detail-panel">
                {seleccionado ? (
                  <UsuarioDetail
                    usuario={seleccionado}
                    onActualizar={handleActualizar}
                    onCerrar={() => setSeleccionado(null)}
                  />
                ) : (
                  <p className="usuarios-vacio">Selecciona un usuario para ver su detalle.</p>
                )}
              </div>
            </div>
          )}
        </main>
      </div>

      {modalNuevo && (
        <UsuarioModal onGuardar={handleUsuarioCreado} onCerrar={() => setModalNuevo(false)} />
      )}
    </div>
  );
}