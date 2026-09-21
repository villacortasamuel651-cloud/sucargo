import { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import PedidoModal from '../components/PedidoModal';
import PedidoDetailModal from '../components/PedidoDetailModal';
import { useAuth } from '../context/AuthContext';
import { listarPedidos, obtenerPedido } from '../services/pedidos';
import './Pedidos.css';
import '../components/InventarioDetailModal.css'; // reusa .inv-estado-badge

const ESTADOS = [
  '', 'CREADO', 'CONFIRMADO', 'STOCK_RESERVADO', 'SIN_STOCK', 'EN_PREPARACION',
  'PICKING_COMPLETADO', 'PACKING_COMPLETADO', 'LISTO_PARA_DESPACHO', 'DESPACHADO', 'CANCELADO',
];

export default function Pedidos() {
  const { usuario } = useAuth();
  const [pedidos, setPedidos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [busqueda, setBusqueda] = useState('');
  const [estadoFiltro, setEstadoFiltro] = useState('');
  const [modalNuevo, setModalNuevo] = useState(false);
  const [detalle, setDetalle] = useState(null);

  useEffect(() => {
    cargarPedidos();
  }, [busqueda, estadoFiltro]);

  async function cargarPedidos() {
    setCargando(true);
    setError('');
    try {
      const data = await listarPedidos({ q: busqueda || undefined, estado: estadoFiltro || undefined });
      setPedidos(data);
    } catch (err) {
      setError('No se pudo conectar con el servidor.');
    } finally {
      setCargando(false);
    }
  }

  async function abrirDetalle(pedido) {
    const completo = await obtenerPedido(pedido.id);
    setDetalle(completo);
  }

  function handleActualizarDetalle(actualizado) {
    setDetalle(actualizado);
    cargarPedidos();
  }

  function formatFecha(fechaISO) {
    return new Date(fechaISO).toLocaleString('es-PE', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' });
  }

  return (
    <div className="dashboard-layout">
      <Sidebar rol={usuario.rol} />

      <div className="dashboard-content">
        <header className="pedidos-header">
          <h2>Pedidos</h2>
          <button className="pedidos-nuevo-btn" onClick={() => setModalNuevo(true)}>+ Nuevo pedido</button>
        </header>

        <main className="pedidos-main">
          <div className="pedidos-filtros">
            <input placeholder="Buscar por código o cliente…" value={busqueda} onChange={(e) => setBusqueda(e.target.value)} />
            <select value={estadoFiltro} onChange={(e) => setEstadoFiltro(e.target.value)}>
              {ESTADOS.map((e) => <option key={e} value={e}>{e || 'Todos los estados'}</option>)}
            </select>
          </div>

          {cargando && <p>Cargando pedidos…</p>}
          {error && <p className="pedidos-error">{error}</p>}
          {!cargando && !error && pedidos.length === 0 && (
            <p className="pedidos-vacio">Todavía no hay pedidos registrados.</p>
          )}

          {!cargando && !error && pedidos.length > 0 && (
            <table className="pedidos-tabla">
              <thead>
                <tr><th>Código</th><th>Cliente</th><th>Fecha</th><th>Items</th><th>Prioridad</th><th>Estado</th></tr>
              </thead>
              <tbody>
                {pedidos.map((p) => (
                  <tr key={p.id} className="pedidos-fila" onClick={() => abrirDetalle(p)}>
                    <td>{p.codigo}</td>
                    <td>{p.cliente.razonSocial}</td>
                    <td>{formatFecha(p.fecha)}</td>
                    <td>{p.totalItems}</td>
                    <td>{p.prioridad}</td>
                    <td><span className={`inv-estado-badge inv-estado-pedido-${p.estado}`}>{p.estado}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </main>
      </div>

      {modalNuevo && (
        <PedidoModal
          onGuardar={() => { setModalNuevo(false); cargarPedidos(); }}
          onCerrar={() => setModalNuevo(false)}
        />
      )}

      {detalle && (
        <PedidoDetailModal pedido={detalle} onActualizar={handleActualizarDetalle} onCerrar={() => setDetalle(null)} />
      )}
    </div>
  );
}