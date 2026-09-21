import { useState, useEffect } from 'react';
import { listarTransportistas } from '../services/transportistas';
import { crearDistribucion } from '../services/distribucion';
import './ClienteModal.css';

export default function DistribucionModal({ onGuardar, onCerrar }) {
  const [transportistas, setTransportistas] = useState([]);
  const [transportistaId, setTransportistaId] = useState('');
  const [fecha, setFecha] = useState('');
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    listarTransportistas({ estado: 'ACTIVO' }).then(setTransportistas).catch(() => setTransportistas([]));
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    if (!transportistaId || !fecha) {
      setError('Transportista y fecha son obligatorios.');
      return;
    }

    setGuardando(true);
    try {
      const dist = await crearDistribucion({ transportistaId, fecha });
      onGuardar(dist);
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo crear la distribución.');
    } finally {
      setGuardando(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <h2>Nueva distribución</h2>

        <form onSubmit={handleSubmit} className="modal-form">
          <label>Transportista</label>
          <select value={transportistaId} onChange={(e) => setTransportistaId(e.target.value)}>
            <option value="">Selecciona un transportista</option>
            {transportistas.map((t) => <option key={t.id} value={t.id}>{t.razonSocial}</option>)}
          </select>

          <label>Fecha</label>
          <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} />

          {error && <p className="modal-error">{error}</p>}

          <div className="modal-acciones">
            <button type="button" className="modal-cancelar" onClick={onCerrar}>Cancelar</button>
            <button type="submit" disabled={guardando}>{guardando ? 'Creando…' : 'Crear'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}