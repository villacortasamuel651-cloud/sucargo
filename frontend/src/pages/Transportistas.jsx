import { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import TransportistaCard from '../components/TransportistaCard';
import TransportistaModal from '../components/TransportistaModal';
import { useAuth } from '../context/AuthContext';
import {
  listarTransportistas,
  crearTransportista,
  editarTransportista,
  desactivarTransportista,
  activarTransportista,
} from '../services/transportistas';
import './Transportistas.css';

export default function Transportistas() {
  const { usuario } = useAuth();
  const [transportistas, setTransportistas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [modalAbierto, setModalAbierto] = useState(false);
  const [editando, setEditando] = useState(null);

  useEffect(() => {
    cargarTransportistas();
  }, []);

  async function cargarTransportistas() {
    setCargando(true);
    setError('');
    try {
      const data = await listarTransportistas();
      setTransportistas(data);
    } catch (err) {
      setError('No se pudo conectar con el servidor.');
    } finally {
      setCargando(false);
    }
  }

  function abrirNuevo() {
    setEditando(null);
    setModalAbierto(true);
  }

  function abrirEditar(t) {
    setEditando(t);
    setModalAbierto(true);
  }

  async function handleGuardar(form, id) {
    if (id) {
      await editarTransportista(id, form);
    } else {
      await crearTransportista(form);
    }
    setModalAbierto(false);
    cargarTransportistas();
  }

  async function handleDesactivar(t) {
    if (!confirm(`¿Desactivar a ${t.razonSocial}?`)) return;
    await desactivarTransportista(t.id);
    cargarTransportistas();
  }

  async function handleActivar(t) {
    await activarTransportista(t.id);
    cargarTransportistas();
  }

  return (
    <div className="dashboard-layout">
      <Sidebar rol={usuario.rol} />

      <div className="dashboard-content">
        <header className="transportistas-header">
          <h2>Transportistas</h2>
          <button className="transportistas-nuevo-btn" onClick={abrirNuevo}>+ Nuevo transportista</button>
        </header>

        <main className="transportistas-main">
          {cargando && <p>Cargando transportistas…</p>}
          {error && <p className="transportistas-error">{error}</p>}
          {!cargando && !error && transportistas.length === 0 && (
            <p className="transportistas-vacio">Todavía no hay transportistas registrados.</p>
          )}

          <div className="transportistas-grid">
            {transportistas.map((t) => (
              <TransportistaCard
                key={t.id}
                transportista={t}
                onEditar={abrirEditar}
                onDesactivar={handleDesactivar}
                onActivar={handleActivar}
              />
            ))}
          </div>
        </main>
      </div>

      {modalAbierto && (
        <TransportistaModal transportista={editando} onGuardar={handleGuardar} onCerrar={() => setModalAbierto(false)} />
      )}
    </div>
  );
}