import { useState, useEffect } from 'react';
import { listarPedidosDisponiblesParaDistribucion } from '../services/pedidos';
import {
  agregarPedido,
  quitarPedido,
  reordenarPedido,
  confirmarDistribucion,
  generarOrdenesTransporte,
} from '../services/distribucion';
import './ClienteDetailModal.css';
import './DistribucionDetailModal.css';

const ROLES_EJECUTORES = ['LOGISTICA', 'DESPACHO'];

export default function DistribucionDetailModal({ distribucion, rolUsuario, onActualizar, onCerrar }) {
  const [pedidosDisponibles, setPedidosDisponibles] = useState([]);
  const [pedidoSeleccionado, setPedidoSeleccionado] = useState('');
  const [error, setError] = useState('');
  const [procesando, setProcesando] = useState(false);
  const [mensaje, setMensaje] = useState('');

  const puedeGestionar = ROLES_EJECUTORES.includes(rolUsuario);
  const abierta = distribucion.estado === 'ABIERTA';

  useEffect(() => {
    if (abierta) {
      listarPedidosDisponiblesParaDistribucion().then(setPedidosDisponibles).catch(() => setPedidosDisponibles([]));
    }
  }, [abierta, distribucion.pedidos.length]);

  async function handleAgregar(e) {
    e.preventDefault();
    setError('');
    if (!pedidoSeleccionado) return;
    try {
      await agregarPedido(distribucion.id, pedidoSeleccionado);
      const pedido = pedidosDisponibles.find((p) => p.id === pedidoSeleccionado);
      onActualizar({
        ...distribucion,
        pedidos: [...distribucion.pedidos, {
          pedidoId: pedido.id, codigo: pedido.codigo, cliente: pedido.cliente.razonSocial,
          secuencia: distribucion.pedidos.length + 1,
        }],
      });
      setPedidoSeleccionado('');
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo agregar el pedido.');
    }
  }

  async function handleQuitar(pedidoId) {
    await quitarPedido(distribucion.id, pedidoId);
    onActualizar({ ...distribucion, pedidos: distribucion.pedidos.filter((p) => p.pedidoId !== pedidoId) });
  }

  async function handleMoverSecuencia(pedidoId, nuevaSecuencia) {
  try {
    const pedidoActual = distribucion.pedidos.find(
      (p) => p.pedidoId === pedidoId
    );

    await reordenarPedido(
      distribucion.id,
      pedidoId,
      nuevaSecuencia
    );

    const pedidosActualizados = distribucion.pedidos.map((p) => {
      if (p.pedidoId === pedidoId) {
        return { ...p, secuencia: nuevaSecuencia };
      }

      if (p.secuencia === nuevaSecuencia) {
        return { ...p, secuencia: pedidoActual.secuencia };
      }

      return p;
    });

    onActualizar({
      ...distribucion,
      pedidos: pedidosActualizados,
    });
  } catch (err) {
    setError(
      err.response?.data?.message || 'No se pudo reordenar.'
    );
  }
  }









  async function handleConfirmar() {
    setProcesando(true);
    setError('');
    try {
      const actualizado = await confirmarDistribucion(distribucion.id);
      onActualizar({ ...distribucion, estado: actualizado.estado });
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo confirmar.');
    } finally {
      setProcesando(false);
    }
  }

  async function handleGenerarOrdenes() {
    setProcesando(true);
    setMensaje('');
    try {
      const ordenes = await generarOrdenesTransporte(distribucion.id);
      setMensaje(`✅ Se generaron ${ordenes.length} órdenes de transporte. Los pedidos avanzaron a LISTO_PARA_DESPACHO.`);
    } catch (err) {
      setMensaje(err.response?.data?.message || 'No se pudieron generar las órdenes.');
    } finally {
      setProcesando(false);
    }
  }

  const pedidosOrdenados = [...distribucion.pedidos].sort((a, b) => a.secuencia - b.secuencia);

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div className="cliente-detail-card distribucion-detail-card" onClick={(e) => e.stopPropagation()}>
        <div className="cliente-detail-header">
          <h2>{distribucion.codigo} — {distribucion.transportista.razonSocial}</h2>
          <button className="cliente-detail-cerrar" onClick={onCerrar}>Cerrar</button>
        </div>

        <div className="cliente-detail-tab">
          <label>Estado</label>
          <span className={`inv-estado-badge dist-estado-${distribucion.estado}`}>{distribucion.estado}</span>

          <label>Fecha</label>
          <p className="cliente-detail-dato">{distribucion.fecha}</p>

          <label>Pedidos en ruta ({pedidosOrdenados.length})</label>
          <ul className="dist-pedidos-lista">
            {pedidosOrdenados.map((p) => (
              <li key={p.pedidoId} className="dist-pedido-item">
                <span className="dist-secuencia">{p.secuencia}</span>
                <div className="dist-pedido-info">
                  <strong>{p.codigo}</strong>
                  <span>{p.cliente}</span>
                </div>
                {abierta && puedeGestionar && (
                  <div className="dist-pedido-acciones">
                    <button onClick={() => handleMoverSecuencia(p.pedidoId, p.secuencia - 1)} disabled={p.secuencia <= 1}>↑</button>
                    <button onClick={() => handleMoverSecuencia(p.pedidoId, p.secuencia + 1)} disabled={p.secuencia >= pedidosOrdenados.length}>↓</button>
                    <button className="dist-quitar" onClick={() => handleQuitar(p.pedidoId)}>Quitar</button>
                  </div>
                )}
              </li>
            ))}
          </ul>

          {abierta && puedeGestionar && (
            <form onSubmit={handleAgregar} className="dist-agregar-form">
              <select value={pedidoSeleccionado} onChange={(e) => setPedidoSeleccionado(e.target.value)}>
                <option value="">Agregar pedido (empaquetados)…</option>
                {pedidosDisponibles.map((p) => (
                  <option key={p.id} value={p.id}>{p.codigo} — {p.cliente.razonSocial}</option>
                ))}
              </select>
              <button type="submit">+ Agregar</button>
            </form>
          )}

          {error && <p className="modal-error">{error}</p>}

          {abierta && puedeGestionar && (
            <button className="usuario-btn-primario" style={{ marginTop: '1.25rem' }} onClick={handleConfirmar} disabled={procesando || pedidosOrdenados.length === 0}>
              {procesando ? 'Confirmando…' : 'Confirmar distribución'}
            </button>
          )}

          {distribucion.estado === 'CONFIRMADA' && puedeGestionar && (
            <>
              <button className="usuario-btn-primario" style={{ marginTop: '1.25rem' }} onClick={handleGenerarOrdenes} disabled={procesando}>
                {procesando ? 'Generando…' : 'Generar órdenes de transporte'}
              </button>
              {mensaje && <p className="dist-mensaje">{mensaje}</p>}
            </>
          )}
        </div>
      </div>
    </div>
  );
}