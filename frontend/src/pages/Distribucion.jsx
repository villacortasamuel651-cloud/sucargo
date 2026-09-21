
import { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import DistribucionModal from '../components/DistribucionModal';
import DistribucionDetailModal from '../components/DistribucionDetailModal';
import { useAuth } from '../context/AuthContext';
import {
  listarDistribuciones,
  obtenerDistribucion,
  eliminarDistribucion
} from '../services/distribucion';
import './Distribucion.css';
import '../components/DistribucionDetailModal.css';

const ROLES_EJECUTORES = ['LOGISTICA', 'DESPACHO'];

export default function Distribucion() {
  const { usuario } = useAuth();
  const puedeCrear = ROLES_EJECUTORES.includes(usuario.rol);

  const [distribuciones, setDistribuciones] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [modalNueva, setModalNueva] = useState(false);
  const [detalle, setDetalle] = useState(null);

  useEffect(() => {
    cargarDistribuciones();
  }, []);

  async function cargarDistribuciones() {
    setCargando(true);
    setError('');

    try {
      const data = await listarDistribuciones();
      setDistribuciones(data);
    } catch (err) {
      setError('No se pudo conectar con el servidor.');
    } finally {
      setCargando(false);
    }
  }

  async function abrirDetalle(d) {
    const completo = await obtenerDistribucion(d.id);
    setDetalle(completo);
  }

  async function handleEliminar(e, distribucion) {
    e.stopPropagation();

    const confirmar = window.confirm(
      `¿Seguro que deseas eliminar la distribución ${distribucion.codigo}?`
    );

    if (!confirmar) return;

    try {
      await eliminarDistribucion(distribucion.id);
      cargarDistribuciones();
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'No se pudo eliminar la distribución.'
      );
    }
  }

  function handleActualizarDetalle(actualizado) {
    setDetalle(actualizado);
    cargarDistribuciones();
  }

  return (
    <div className="dashboard-layout">
      <Sidebar rol={usuario.rol} />

      <div className="dashboard-content">
        <header className="distribucion-header">
          <h2>Distribución</h2>

          {puedeCrear && (
            <button
              className="distribucion-nuevo-btn"
              onClick={() => setModalNueva(true)}
            >
              + Nueva distribución
            </button>
          )}
        </header>

        <main className="distribucion-main">
          {cargando && <p>Cargando…</p>}

          {error && (
            <p className="distribucion-error">
              {error}
            </p>
          )}

          {!cargando &&
            !error &&
            distribuciones.length === 0 && (
              <p className="distribucion-vacio">
                Todavía no hay distribuciones registradas.
              </p>
            )}

          <div className="distribucion-grid">
            {distribuciones.map((d) => (
              <div
                key={d.id}
                className="distribucion-card"
                onClick={() => abrirDetalle(d)}
              >
                <div className="distribucion-card-header">
                  <strong>{d.codigo}</strong>

                  <span
                    className={`inv-estado-badge dist-estado-${d.estado}`}
                  >
                    {d.estado}
                  </span>
                </div>

                <p>{d.transportista.razonSocial}</p>

                <p>
                  {d.fecha} — {d.totalPedidos} pedidos
                </p>

                {d.estado === 'ABIERTA' && (
                  <button
                    className="distribucion-eliminar-btn"
                    onClick={(e) => handleEliminar(e, d)}
                  >
                    Eliminar
                  </button>
                )}
              </div>
            ))}
          </div>
        </main>
      </div>

      {modalNueva && (
        <DistribucionModal
          onGuardar={() => {
            setModalNueva(false);
            cargarDistribuciones();
          }}
          onCerrar={() => setModalNueva(false)}
        />
      )}

      {detalle && (
        <DistribucionDetailModal
          distribucion={detalle}
          rolUsuario={usuario.rol}
          onActualizar={handleActualizarDetalle}
          onCerrar={() => setDetalle(null)}
        />
      )}
    </div>
  );
}

