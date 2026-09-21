
import { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import MovimientoModal from '../components/MovimientoModal';
import InventarioDetailModal from '../components/InventarioDetailModal';
import { useAuth } from '../context/AuthContext';
import { listarInventario } from '../services/inventario';
import { listarAlmacenes } from '../services/almacenes';
import './Inventario.css';

const ESTADOS = [
  { value: '', label: 'Todos los estados' },
  { value: 'NORMAL', label: 'Normal' },
  { value: 'BAJO_STOCK', label: 'Bajo stock' },
  { value: 'SIN_STOCK', label: 'Sin stock' },
];

export default function Inventario() {
  const { usuario } = useAuth();

  const [items, setItems] = useState([]);
  const [almacenes, setAlmacenes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  const [busqueda, setBusqueda] = useState('');
  const [almacenId, setAlmacenId] = useState('');
  const [estadoFiltro, setEstadoFiltro] = useState('');

  const [modalMovimiento, setModalMovimiento] = useState(false);
  const [detalleSeleccionado, setDetalleSeleccionado] = useState(null);

  useEffect(() => {
    listarAlmacenes()
      .then(setAlmacenes)
      .catch(() => setAlmacenes([]));
  }, []);

  useEffect(() => {
    cargarInventario();
  }, [busqueda, almacenId]);

  async function cargarInventario() {
    setCargando(true);
    setError('');

    try {
      const data = await listarInventario({
        q: busqueda || undefined,
        almacenId: almacenId || undefined,
      });

      setItems(data);
    } catch (err) {
      setError('No se pudo conectar con el servidor.');
    } finally {
      setCargando(false);
    }
  }

  // El backend ya devuelve el estado calculado.
  // Aquí solamente filtramos las filas que se muestran.
  const itemsFiltrados = estadoFiltro
    ? items.filter((i) => i.estado === estadoFiltro)
    : items;

  // Resumen basado en los datos que devuelve la API.
  const resumen = {
    productos: new Set(items.map((i) => i.producto.id)).size,
    stockFisico: items.reduce(
      (acc, i) => acc + i.stockFisico,
      0
    ),
    stockReservado: items.reduce(
      (acc, i) => acc + i.stockReservado,
      0
    ),
    stockDisponible: items.reduce(
      (acc, i) => acc + i.stockDisponible,
      0
    ),
    bajoStock: items.filter(
      (i) =>
        i.estado === 'BAJO_STOCK' ||
        i.estado === 'SIN_STOCK'
    ).length,
  };

  return (
    <div className="dashboard-layout">
      <Sidebar rol={usuario.rol} />

      <div className="dashboard-content">

        <header className="inventario-header">
          <h2>Inventario</h2>

          <button
            className="inventario-nuevo-btn"
            onClick={() => setModalMovimiento(true)}
          >
            + Registrar movimiento
          </button>
        </header>

        <main className="inventario-main">

          {/* RESUMEN */}
          <div className="dashboard-stats">

            <div
              className="dashboard-stat-card"
              style={{ '--stat-color': '#6366F1' }}
            >
              <div className="dashboard-stat-valor">
                {resumen.productos}
              </div>
              <div className="dashboard-stat-label">
                Productos con stock
              </div>
            </div>

            <div
              className="dashboard-stat-card"
              style={{ '--stat-color': '#15803D' }}
            >
              <div className="dashboard-stat-valor">
                {resumen.stockFisico}
              </div>
              <div className="dashboard-stat-label">
                Stock físico
              </div>
            </div>

            <div
              className="dashboard-stat-card"
              style={{ '--stat-color': '#B45309' }}
            >
              <div className="dashboard-stat-valor">
                {resumen.stockReservado}
              </div>
              <div className="dashboard-stat-label">
                Stock reservado
              </div>
            </div>

            <div
              className="dashboard-stat-card"
              style={{ '--stat-color': '#06B6D4' }}
            >
              <div className="dashboard-stat-valor">
                {resumen.stockDisponible}
              </div>
              <div className="dashboard-stat-label">
                Stock disponible
              </div>
            </div>

            <div
              className="dashboard-stat-card"
              style={{ '--stat-color': '#B91C1C' }}
            >
              <div className="dashboard-stat-valor">
                {resumen.bajoStock}
              </div>
              <div className="dashboard-stat-label">
                Bajo stock
              </div>
            </div>

          </div>

          {/* FILTROS */}
          <div className="inventario-filtros">

            <input
              placeholder="Buscar producto o SKU…"
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
            />

            <select
              value={almacenId}
              onChange={(e) => setAlmacenId(e.target.value)}
            >
              <option value="">
                Todos los almacenes
              </option>

              {almacenes.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.nombre}
                </option>
              ))}
            </select>

            <select
              value={estadoFiltro}
              onChange={(e) => setEstadoFiltro(e.target.value)}
            >
              {ESTADOS.map((e) => (
                <option key={e.value} value={e.value}>
                  {e.label}
                </option>
              ))}
            </select>

          </div>

          {/* ESTADOS DE CARGA */}
          {cargando && (
            <p>Cargando inventario…</p>
          )}

          {error && (
            <p className="inventario-error">
              {error}
            </p>
          )}

          {/* SIN RESULTADOS */}
          {!cargando &&
            !error &&
            itemsFiltrados.length === 0 && (
              <p className="inventario-vacio">
                No hay inventario registrado todavía.
              </p>
            )}

          {/* TABLA */}
          {!cargando &&
            !error &&
            itemsFiltrados.length > 0 && (

              <table className="inventario-tabla">

                <thead>
                  <tr>
                    <th>Producto</th>
                    <th>SKU</th>
                    <th>Almacén</th>
                    <th>Físico</th>
                    <th>Reservado</th>
                    <th>Disponible</th>
                    <th>Estado</th>
                  </tr>
                </thead>

                <tbody>
                  {itemsFiltrados.map((i) => (
                    <tr
                      key={i.id}
                      onClick={() => setDetalleSeleccionado(i)}
                      className="inventario-fila"
                    >
                      <td>{i.producto.nombre}</td>
                      <td>{i.producto.sku}</td>
                      <td>{i.almacen.nombre}</td>
                      <td>{i.stockFisico}</td>
                      <td>{i.stockReservado}</td>
                      <td>{i.stockDisponible}</td>
                      <td>
                        <span
                          className={`inv-estado-badge inv-estado-${i.estado}`}
                        >
                          {i.estado}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>

              </table>
            )}

        </main>
      </div>

      {/* MODAL DE MOVIMIENTO */}
      {modalMovimiento && (
        <MovimientoModal
          onGuardar={() => {
            setModalMovimiento(false);
            cargarInventario();
          }}
          onCerrar={() => setModalMovimiento(false)}
        />
      )}

      {/* DETALLE DEL INVENTARIO */}
      {detalleSeleccionado && (
        <InventarioDetailModal
          item={detalleSeleccionado}
          onCerrar={() => setDetalleSeleccionado(null)}
        />
      )}

    </div>
  );
}

