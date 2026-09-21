import { useState } from 'react';
import DetailTabs from './DetailTabs';
import { agregarPuntoEntrega, desactivarPuntoEntrega } from '../services/clientes';
import './ClienteDetailModal.css';

export default function ClienteDetailModal({ cliente, onActualizar, onCerrar }) {
  const [direccion, setDireccion] = useState('');
  const [distrito, setDistrito] = useState('');
  const [referencia, setReferencia] = useState('');
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  async function handleAgregarPunto(e) {
    e.preventDefault();
    if (!direccion.trim()) return;

    setError('');
    setGuardando(true);
    try {
      const nuevo = await agregarPuntoEntrega(cliente.id, { direccion, distrito, referencia });
      onActualizar({ ...cliente, puntosEntrega: [...(cliente.puntosEntrega || []), nuevo] });
      setDireccion(''); setDistrito(''); setReferencia('');
    } catch (err) {
      setError('No se pudo agregar el punto de entrega.');
    } finally {
      setGuardando(false);
    }
  }

  async function handleDesactivarPunto(puntoId) {
    if (!confirm('¿Desactivar este punto de entrega?')) return;
    await desactivarPuntoEntrega(cliente.id, puntoId);
    onActualizar({
      ...cliente,
      puntosEntrega: cliente.puntosEntrega.filter((p) => p.id !== puntoId),
    });
  }

  const tabs = [
    {
      id: 'info',
      label: 'Datos generales',
      content: (
        <div className="cliente-detail-tab">
          <label>Razón social</label>
          <p className="cliente-detail-dato">{cliente.razonSocial}</p>
          <label>RUC / DNI</label>
          <p className="cliente-detail-dato">{cliente.rucDni}</p>
          <label>Teléfono</label>
          <p className="cliente-detail-dato">{cliente.telefono || '—'}</p>
          <label>Estado</label>
          <span className={`cliente-detail-badge ${cliente.estado === 'INACTIVO' ? 'cliente-detail-badge-inactivo' : ''}`}>
            {cliente.estado}
          </span>
        </div>
      ),
    },
    {
      id: 'puntos',
      label: `Puntos de entrega (${cliente.puntosEntrega?.length || 0})`,
      content: (
        <div className="cliente-detail-tab">
          {(cliente.puntosEntrega?.length || 0) === 0 ? (
            <p className="cliente-detail-vacio">Todavía no hay puntos de entrega.</p>
          ) : (
            <ul className="cliente-detail-puntos-lista">
              {cliente.puntosEntrega.map((p) => (
                <li key={p.id} className="cliente-detail-punto-item">
                  <div>
                    <strong>{p.direccion}</strong>
                    {p.distrito && <span> — {p.distrito}</span>}
                    {p.referencia && <div className="cliente-detail-punto-ref">{p.referencia}</div>}
                  </div>
                  <button onClick={() => handleDesactivarPunto(p.id)}>Desactivar</button>
                </li>
              ))}
            </ul>
          )}

          <form onSubmit={handleAgregarPunto} className="cliente-detail-form-punto">
            <input placeholder="Dirección" value={direccion} onChange={(e) => setDireccion(e.target.value)} />
            <input placeholder="Distrito (opcional)" value={distrito} onChange={(e) => setDistrito(e.target.value)} />
            <input placeholder="Referencia (opcional)" value={referencia} onChange={(e) => setReferencia(e.target.value)} />
            <button type="submit" disabled={guardando}>{guardando ? 'Agregando…' : '+ Agregar punto de entrega'}</button>
          </form>
          {error && <p className="cliente-detail-error">{error}</p>}
        </div>
      ),
    },
  ];

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div className="cliente-detail-card" onClick={(e) => e.stopPropagation()}>
        <div className="cliente-detail-header">
          <h2>{cliente.razonSocial}</h2>
          <button className="cliente-detail-cerrar" onClick={onCerrar}>Cerrar</button>
        </div>
        <DetailTabs tabs={tabs} tabInicial="puntos" />
      </div>
    </div>
  );
}
