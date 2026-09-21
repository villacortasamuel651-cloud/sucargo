
import { useState, useEffect } from 'react';
import { listarProductos } from '../services/productos';
import { listarAlmacenes } from '../services/almacenes';
import { registrarMovimiento } from '../services/inventario';
import './ClienteModal.css';

const TIPOS = [
  { value: 'ENTRADA', label: 'Entrada' },
  { value: 'SALIDA', label: 'Salida' },
  { value: 'AJUSTE', label: 'Ajuste (conteo físico)' },
  { value: 'RESERVA', label: 'Reserva' },
  { value: 'LIBERACION', label: 'Liberación' },
];

export default function MovimientoModal({ onGuardar, onCerrar }) {
  const [productos, setProductos] = useState([]);
  const [almacenes, setAlmacenes] = useState([]);

  const [productoId, setProductoId] = useState('');
  const [almacenId, setAlmacenId] = useState('');
  const [tipo, setTipo] = useState('ENTRADA');
  const [cantidad, setCantidad] = useState('');
  const [motivo, setMotivo] = useState('');

  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    listarProductos()
      .then(setProductos)
      .catch(() => setProductos([]));

    listarAlmacenes()
      .then(setAlmacenes)
      .catch(() => setAlmacenes([]));
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (!productoId || !almacenId || !tipo || !cantidad) {
      setError('Producto, almacén, tipo y cantidad son obligatorios.');
      return;
    }

    if (Number(cantidad) <= 0) {
      setError('La cantidad debe ser mayor que cero.');
      return;
    }

    setGuardando(true);

    try {
      await registrarMovimiento({
        productoId,
        almacenId,
        tipo,
        cantidad: Number(cantidad),
        motivo,
      });

      onGuardar();
    } catch (err) {
      // El backend y el Stored Procedure son quienes
      // validan si el movimiento puede realizarse.
      setError(
        err.response?.data?.message ||
        'No se pudo registrar el movimiento.'
      );
    } finally {
      setGuardando(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div
        className="modal-card"
        onClick={(e) => e.stopPropagation()}
      >
        <h2>Registrar movimiento</h2>

        <form onSubmit={handleSubmit} className="modal-form">

          <label>Producto</label>

          <select
            value={productoId}
            onChange={(e) => setProductoId(e.target.value)}
          >
            <option value="">
              Selecciona un producto
            </option>

            {productos.map((p) => (
              <option key={p.id} value={p.id}>
                {p.sku} — {p.nombre}
              </option>
            ))}
          </select>


          <label>Almacén</label>

          <select
            value={almacenId}
            onChange={(e) => setAlmacenId(e.target.value)}
          >
            <option value="">
              Selecciona un almacén
            </option>

            {almacenes.map((a) => (
              <option key={a.id} value={a.id}>
                {a.nombre}
              </option>
            ))}
          </select>


          <label>Tipo de movimiento</label>

          <select
            value={tipo}
            onChange={(e) => setTipo(e.target.value)}
          >
            {TIPOS.map((t) => (
              <option key={t.value} value={t.value}>
                {t.label}
              </option>
            ))}
          </select>


          <label>Cantidad</label>

          <input
            type="number"
            min="1"
            value={cantidad}
            onChange={(e) => setCantidad(e.target.value)}
          />


          <label>Motivo</label>

          <input
            value={motivo}
            onChange={(e) => setMotivo(e.target.value)}
            placeholder="Ej. Compra a proveedor"
          />


          {error && (
            <p className="modal-error">
              {error}
            </p>
          )}


          <div className="modal-acciones">

            <button
              type="button"
              className="modal-cancelar"
              onClick={onCerrar}
            >
              Cancelar
            </button>

            <button
              type="submit"
              disabled={guardando}
            >
              {guardando
                ? 'Registrando…'
                : 'Registrar'}
            </button>

          </div>

        </form>
      </div>
    </div>
  );
}

