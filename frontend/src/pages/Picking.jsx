import { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import GenerarPickingModal from '../components/GenerarPickingModal';
import PickingDetailModal from '../components/PickingDetailModal';
import { useAuth } from '../context/AuthContext';
import { listarPicking, obtenerPicking } from '../services/picking';
import './Picking.css';
import '../components/PickingDetailModal.css';

const ROLES_EJECUTORES = ['ALMACENERO', 'SUPERVISOR_ALMACEN'];
const ESTADOS = ['', 'PENDIENTE', 'EN_PROCESO', 'COMPLETADO', 'COMPLETADO_CON_INCIDENCIAS'];

export default function Picking() {
  const { usuario } = useAuth();
  const puedeGenerar = ROLES_EJECUTORES.includes(usuario.rol);

  const [pickings, setPickings] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [estadoFiltro, setEstadoFiltro] = useState('');
  const [modalGenerar, setModalGenerar] = useState(false);
  const [detalle, setDetalle] = useState(null);

  useEffect(() => {
    cargarPickings();
  }, [estadoFiltro]);

  async function cargarPickings() {
    setCargando(true);
    setError('');
    try {
      const data = await listarPicking({ estado: estadoFiltro || undefined });
      setPickings(data);
    } catch (err) {
      setError('No se pudo conectar con el servidor.');
    } finally {
      setCargando(false);
    }
  }

  async function abrirDetalle(p) {
    const completo = await obtenerPicking(p.id);
    setDetalle(completo);
  }

  function handleActualizarDetalle(actualizado) {
    setDetalle(actualizado);
    cargarPickings();
  }

  function formatFecha(fechaISO) {
    if (!fechaISO) return '—';
    return new Date(fechaISO).toLocaleString('es-PE', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' });
  }

  return (
    <div className="dashboard-layout">
      <Sidebar rol={usuario.rol} />

      <div className="dashboard-content">
        <header className="picking-header">
          <h2>Picking</h2>
          {puedeGenerar && (
            <button className="picking-nuevo-btn" onClick={() => setModalGenerar(true)}>+ Generar picking</button>
          )}
        </header>

        <main className="picking-main">
          <div className="picking-filtros">
            <select value={estadoFiltro} onChange={(e) => setEstadoFiltro(e.target.value)}>
              {ESTADOS.map((e) => <option key={e} value={e}>{e || 'Todos los estados'}</option>)}
            </select>
          </div>

          {cargando && <p>Cargando…</p>}
          {error && <p className="picking-error">{error}</p>}
          {!cargando && !error && pickings.length === 0 && (
            <p className="picking-vacio">No hay pickings registrados todavía.</p>
          )}

          {!cargando && !error && pickings.length > 0 && (
            <table className="picking-tabla">
              <thead>
                <tr><th>Pedido</th><th>Estado</th><th>Asignado a</th><th>Inicio</th><th>Fin</th></tr>
              </thead>
              <tbody>
                {pickings.map((p) => (
                  <tr key={p.id} className="picking-fila" onClick={() => abrirDetalle(p)}>
                    <td>{p.pedido.codigo}</td>
                    <td><span className={`inv-estado-badge picking-estado-${p.estado}`}>{p.estado}</span></td>
                    <td>{p.usuario || '—'}</td>
                    <td>{formatFecha(p.fechaInicio)}</td>
                    <td>{formatFecha(p.fechaFin)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </main>
      </div>

      {modalGenerar && (
        <GenerarPickingModal
          onGuardar={() => { setModalGenerar(false); cargarPickings(); }}
          onCerrar={() => setModalGenerar(false)}
        />
      )}

      {detalle && (
        <PickingDetailModal picking={detalle} onActualizar={handleActualizarDetalle} onCerrar={() => setDetalle(null)} />
      )}
    </div>
  );
}