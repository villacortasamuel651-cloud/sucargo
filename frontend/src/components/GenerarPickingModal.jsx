import { useState, useEffect } from 'react';
import { listarPedidos } from '../services/pedidos';
import { generarPicking } from '../services/picking';
import './ClienteModal.css';

export default function GenerarPickingModal({ onGuardar, onCerrar }) {
  const [pedidos, setPedidos] = useState([]);
  const [pedidoId, setPedidoId] = useState('');
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    // Solo pedidos con stock ya reservado pueden pasar a picking
    listarPedidos({ estado: 'STOCK_RESERVADO' }).then(setPedidos).catch(() => setPedidos([]));
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    if (!pedidoId) { setError('Selecciona un pedido.'); return; }

    setGuardando(true);
    try {
      const picking = await generarPicking(pedidoId);
      onGuardar(picking);
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo generar el picking.');
    } finally {
      setGuardando(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <h2>Generar picking</h2>

        <form onSubmit={handleSubmit} className="modal-form">
          <label>Pedido (con stock reservado)</label>
          {pedidos.length === 0 ? (
            <p className="modal-error" style={{ color: '#8a8f98' }}>
              No hay pedidos con stock reservado listos para picking.
            </p>
          ) : (
            <select value={pedidoId} onChange={(e) => setPedidoId(e.target.value)}>
              <option value="">Selecciona un pedido</option>
              {pedidos.map((p) => (
                <option key={p.id} value={p.id}>{p.codigo} — {p.cliente.razonSocial}</option>
              ))}
            </select>
          )}

          {error && <p className="modal-error">{error}</p>}

          <div className="modal-acciones">
            <button type="button" className="modal-cancelar" onClick={onCerrar}>Cancelar</button>
            <button type="submit" disabled={guardando || pedidos.length === 0}>
              {guardando ? 'Generando…' : 'Generar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}