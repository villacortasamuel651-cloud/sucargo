import { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import { useAuth } from '../context/AuthContext';
import {
  listarOrdenes,
  obtenerOrden,
  anularOrden
} from '../services/ordenestransporte';
import './Ordenestransporte.css';

const ROLES_EJECUTORES = ['LOGISTICA', 'DESPACHO'];

export default function OrdenesTransporte() {
  const { usuario } = useAuth();
  const puedeAnular = ROLES_EJECUTORES.includes(usuario.rol);

  const [ordenes, setOrdenes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  const [detalle, setDetalle] = useState(null);
  const [cargandoDetalle, setCargandoDetalle] = useState(false);

  useEffect(() => {
    cargarOrdenes();
  }, []);

  async function cargarOrdenes() {
    setCargando(true);
    setError('');

    try {
      const data = await listarOrdenes();
      setOrdenes(data);
    } catch (err) {
      setError('No se pudo conectar con el servidor.');
    } finally {
      setCargando(false);
    }
  }

  async function abrirDetalle(id) {
    setCargandoDetalle(true);
    setError('');

    try {
      const data = await obtenerOrden(id);
      setDetalle(data);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'No se pudo cargar el detalle de la orden.'
      );
    } finally {
      setCargandoDetalle(false);
    }
  }

  async function handleAnular(e, id) {
    e.stopPropagation();

    if (
      !confirm(
        '¿Anular esta orden de transporte? El pedido volverá a PACKING_COMPLETADO.'
      )
    ) {
      return;
    }

    try {
      await anularOrden(id);

      if (detalle?.id === id) {
        setDetalle(null);
      }

      await cargarOrdenes();
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'No se pudo anular la orden de transporte.'
      );
    }
  }

  return (
    <div className="dashboard-layout">
      <Sidebar rol={usuario.rol} />

      <div className="dashboard-content">
        <header className="ot-header">
          <h2>Órdenes de Transporte</h2>
        </header>

        <main className="ot-main">
          <p className="ot-nota">
            Las órdenes se generan automáticamente al confirmar una
            distribución (módulo Distribución). Aquí solo se consultan o
            anulan.
          </p>

          {cargando && <p>Cargando…</p>}

          {error && (
            <p className="ot-error">
              {error}
            </p>
          )}

          {!cargando && !error && ordenes.length === 0 && (
            <p className="ot-vacio">
              Todavía no hay órdenes de transporte generadas.
            </p>
          )}

          {!cargando && !error && ordenes.length > 0 && (
            <table className="ot-tabla">
              <thead>
                <tr>
                  <th>Número</th>
                  <th>Pedido</th>
                  <th>Destino</th>
                  <th>Bultos</th>
                  <th>Peso</th>
                  <th>Estado</th>
                  {puedeAnular && <th></th>}
                </tr>
              </thead>

              <tbody>
                {ordenes.map((o) => (
                  <tr
                    key={o.id}
                    className="ot-fila"
                    onClick={() => abrirDetalle(o.id)}
                  >
                    <td>
                      <strong className="ot-numero">
                        {o.numero}
                      </strong>
                    </td>

                    <td>
                      {o.pedido?.codigo || '—'}
                    </td>

                    <td>
                      {o.destino || '—'}
                    </td>

                    <td>
                      {o.totalBultos ?? 0}
                    </td>

                    <td>
                      {o.pesoTotal
                        ? `${o.pesoTotal} kg`
                        : '—'}
                    </td>

                    <td>
                      <span
                        className={`inv-estado-badge ot-estado-${o.estado}`}
                      >
                        {o.estado}
                      </span>
                    </td>

                    {puedeAnular && (
                      <td>
                        {o.estado === 'EMITIDA' && (
                          <button
                            className="ot-anular-btn"
                            onClick={(e) =>
                              handleAnular(e, o.id)
                            }
                          >
                            Anular
                          </button>
                        )}
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </main>
      </div>

      {cargandoDetalle && (
        <div className="ot-modal-overlay">
          <div className="ot-modal">
            <p>Cargando detalle…</p>
          </div>
        </div>
      )}

      {detalle && !cargandoDetalle && (
        <div
          className="ot-modal-overlay"
          onClick={() => setDetalle(null)}
        >
          <div
            className="ot-modal"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="ot-modal-header">
              <div>
                <span className="ot-modal-label">
                  Orden de transporte
                </span>

                <h3>{detalle.numero}</h3>
              </div>

              <button
                className="ot-modal-cerrar"
                onClick={() => setDetalle(null)}
              >
                Cerrar
              </button>
            </div>

            <div className="ot-modal-estado">
              <span>Estado</span>

              <span
                className={`inv-estado-badge ot-estado-${detalle.estado}`}
              >
                {detalle.estado}
              </span>
            </div>

            <div className="ot-detalle-grid">
              <div className="ot-detalle-seccion">
                <h4>Distribución</h4>

                <div className="ot-detalle-item">
                  <span>Transportista</span>
                  <strong>
                    {detalle.transportista || '—'}
                  </strong>
                </div>
              </div>

              <div className="ot-detalle-seccion">
                <h4>Pedido</h4>

                <div className="ot-detalle-item">
                  <span>Código</span>
                  <strong>
                    {detalle.pedido?.codigo || '—'}
                  </strong>
                </div>
              </div>

              <div className="ot-detalle-seccion">
                <h4>Origen</h4>

                <div className="ot-detalle-item">
                  <span>Almacén</span>
                  <strong>
                    {detalle.origenAlmacen?.nombre || '—'}
                  </strong>
                </div>
              </div>

              <div className="ot-detalle-seccion">
                <h4>Destino</h4>

                <div className="ot-detalle-item">
                  <span>Dirección</span>
                  <strong>
                    {detalle.destino?.direccion || '—'}
                  </strong>
                </div>

                <div className="ot-detalle-item">
                  <span>Distrito</span>
                  <strong>
                    {detalle.destino?.distrito || '—'}
                  </strong>
                </div>
              </div>

              <div className="ot-detalle-seccion">
                <h4>Carga</h4>

                <div className="ot-detalle-datos">
                  <div>
                    <span>Bultos</span>
                    <strong>
                      {detalle.totalBultos ?? 0}
                    </strong>
                  </div>

                  <div>
                    <span>Peso total</span>
                    <strong>
                      {detalle.pesoTotal
                        ? `${detalle.pesoTotal} kg`
                        : '—'}
                    </strong>
                  </div>
                </div>
              </div>

              <div className="ot-detalle-seccion">
                <h4>Ventana de entrega</h4>

                <div className="ot-detalle-datos">
                  <div>
                    <span>Inicio</span>
                    <strong>
                      {detalle.ventanaInicio || '—'}
                    </strong>
                  </div>

                  <div>
                    <span>Fin</span>
                    <strong>
                      {detalle.ventanaFin || '—'}
                    </strong>
                  </div>
                </div>
              </div>
            </div>

            {puedeAnular && detalle.estado === 'EMITIDA' && (
              <div className="ot-modal-footer">
                <button
                  className="ot-anular-btn"
                  onClick={(e) =>
                    handleAnular(e, detalle.id)
                  }
                >
                  Anular orden
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
