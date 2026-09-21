import { useState, useEffect } from 'react';
import DetailTabs from './DetailTabs';
import { obtenerMovimientos } from '../services/inventario';
import './InventarioDetailModal.css';

export default function InventarioDetailModal({ item, onCerrar }) {
  const [movimientos, setMovimientos] = useState([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    obtenerMovimientos(item.id)
      .then(setMovimientos)
      .catch(() => setMovimientos([]))
      .finally(() => setCargando(false));
  }, [item.id]);

  function formatFecha(fechaISO) {
    return new Date(fechaISO).toLocaleString('es-PE', {
      day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
    });
  }

  const tabs = [
    {
      id: 'datos',
      label: 'Datos generales',
      content: (
        <div className="inv-detail-tab">
          <label>Producto</label>
          <p className="inv-detail-dato">{item.producto.nombre} ({item.producto.sku})</p>
          <label>Almacén</label>
          <p className="inv-detail-dato">{item.almacen.nombre}</p>
         

          <div className="inv-detail-stock-grid">
            <div className="inv-detail-stock-item">
              <span className="inv-detail-stock-label">Físico</span>
              <span className="inv-detail-stock-valor">{item.stockFisico}</span>
            </div>
            <div className="inv-detail-stock-item">
              <span className="inv-detail-stock-label">Reservado</span>
              <span className="inv-detail-stock-valor">{item.stockReservado}</span>
            </div>
            <div className="inv-detail-stock-item">
              <span className="inv-detail-stock-label">Disponible</span>
              <span className="inv-detail-stock-valor">{item.stockDisponible}</span>
            </div>
          </div>

          <label>Estado</label>
          <span className={`inv-estado-badge inv-estado-${item.estado}`}>{item.estado}</span>
        </div>
      ),
    },
    {
      id: 'historial',
      label: 'Historial de movimientos',
      content: (
        <div className="inv-detail-tab">
          {cargando && <p className="inv-detail-vacio">Cargando…</p>}
          {!cargando && movimientos.length === 0 && (
            <p className="inv-detail-vacio">No hay movimientos registrados todavía.</p>
          )}
          {!cargando && movimientos.length > 0 && (
            <table className="inv-mov-tabla">
              <thead>
                <tr><th>Fecha</th><th>Tipo</th><th>Cantidad</th><th>Motivo</th><th>Usuario</th></tr>
              </thead>
              <tbody>
                {movimientos.map((m) => (
                  <tr key={m.id}>
                    <td>{formatFecha(m.fecha)}</td>
                    <td>
                      <span className={`inv-tipo-badge inv-tipo-${m.tipo}`}>{m.tipo}</span>
                    </td>
                    <td>{m.cantidad}</td>
                    <td>{m.motivo || '—'}</td>
                    <td>{m.usuario}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div className="inv-detail-card" onClick={(e) => e.stopPropagation()}>
        <div className="inv-detail-header">
          <h2>{item.producto.nombre}</h2>
          <button className="inv-detail-cerrar" onClick={onCerrar}>Cerrar</button>
        </div>
        <DetailTabs tabs={tabs} tabInicial="historial" />
      </div>
    </div>
  );
}