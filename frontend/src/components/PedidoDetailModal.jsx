import { useState } from 'react';
import { cancelarPedido } from '../services/pedidos';
import './ClienteDetailModal.css';

const ESTADOS_CANCELABLES = ['CREADO', 'STOCK_RESERVADO', 'SIN_STOCK'];

export default function PedidoDetailModal({ pedido, onActualizar, onCerrar }) {
  const [cancelando, setCancelando] = useState(false);
  const puedeCancelar = ESTADOS_CANCELABLES.includes(pedido.estado);

  async function handleCancelar() {
    if (!confirm(`¿Cancelar el pedido ${pedido.codigo}?`)) return;
    setCancelando(true);
    try {
      const actualizado = await cancelarPedido(pedido.id);
      onActualizar(actualizado);
    } catch (err) {
      alert(err.response?.data?.message || 'No se pudo cancelar el pedido.');
    } finally {
      setCancelando(false);
    }
  }

  function formatFecha(fechaISO) {
    return new Date(fechaISO).toLocaleString('es-PE', {
      day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
    });
  }

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div className="cliente-detail-card" onClick={(e) => e.stopPropagation()}>
        <div className="cliente-detail-header">
          <h2>{pedido.codigo}</h2>
          <button className="cliente-detail-cerrar" onClick={onCerrar}>Cerrar</button>
        </div>

        <div className="cliente-detail-tab">
          <label>Cliente</label>
          <p className="cliente-detail-dato">{pedido.cliente.razonSocial}</p>

          <label>Punto de entrega</label>
          <p className="cliente-detail-dato">
            {pedido.puntoEntrega.direccion}{pedido.puntoEntrega.distrito ? ` — ${pedido.puntoEntrega.distrito}` : ''}
          </p>

          <label>Estado</label>
          <span className={`inv-estado-badge inv-estado-pedido-${pedido.estado}`}>{pedido.estado}</span>

          <label>Prioridad</label>
          <p className="cliente-detail-dato">{pedido.prioridad}</p>

          <label>Fecha</label>
          <p className="cliente-detail-dato">{formatFecha(pedido.fecha)}</p>

          <label>Productos</label>
          <table className="inv-mov-tabla">
            <thead>
              <tr><th>Producto</th><th>SKU</th><th>Cantidad</th><th>Reservado</th></tr>
            </thead>
            <tbody>
              {pedido.items.map((it) => (
                <tr key={it.id}>
                  <td>{it.producto.nombre}</td>
                  <td>{it.producto.sku}</td>
                  <td>{it.cantidad}</td>
                  <td>{it.cantidadReservada}</td>
                </tr>
              ))}
            </tbody>
          </table>

          {puedeCancelar && (
            <button
              className="usuario-btn-peligro"
              style={{ marginTop: '1.25rem' }}
              onClick={handleCancelar}
              disabled={cancelando}
            >
              {cancelando ? 'Cancelando…' : 'Cancelar pedido'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}