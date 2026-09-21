import { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import BultoDetailModal from '../components/BultoDetailModal';
import { useAuth } from '../context/AuthContext';
import { listarPedidos } from '../services/pedidos';
import {
  listarBultos,
  obtenerBulto,
  crearBulto,
  completarPacking,
  eliminarBulto
} from '../services/bultos';
import './Packing.css';
import '../components/PickingDetailModal.css';

const ROLES_EJECUTORES = ['SUPERVISOR_ALMACEN'];

export default function Packing() {
  const { usuario } = useAuth();
  const puedeGestionar = ROLES_EJECUTORES.includes(usuario.rol);

  const [pedidos, setPedidos] = useState([]);
  const [pedidoId, setPedidoId] = useState('');
  const [bultos, setBultos] = useState([]);
  const [cargandoBultos, setCargandoBultos] = useState(false);
  const [error, setError] = useState('');
  const [detalle, setDetalle] = useState(null);
  const [mensajeCompletar, setMensajeCompletar] = useState('');

  // Información del packing que acaba de completarse
  const [packingCompletado, setPackingCompletado] = useState(null);

  useEffect(() => {
    cargarPedidos();
  }, []);

  useEffect(() => {
    if (!pedidoId) {
      setBultos([]);
      return;
    }

    cargarBultos();
  }, [pedidoId]);

  async function cargarPedidos() {
    try {
      const data = await listarPedidos({
        estado: 'PICKING_COMPLETADO'
      });

      setPedidos(data);
    } catch (err) {
      setPedidos([]);
    }
  }

  async function cargarBultos() {
    setCargandoBultos(true);
    setError('');

    try {
      const data = await listarBultos(pedidoId);
      setBultos(data);
    } catch (err) {
      setError('No se pudo conectar con el servidor.');
    } finally {
      setCargandoBultos(false);
    }
  }

  async function handleNuevoBulto() {
    try {
      await crearBulto(pedidoId);
      await cargarBultos();
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'No se pudo crear el bulto.'
      );
    }
  }

  async function handleEliminarBulto(e, bultoId) {
    e.stopPropagation();

    if (!confirm('¿Eliminar este bulto vacío?')) return;

    try {
      await eliminarBulto(bultoId);
      await cargarBultos();
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'No se pudo eliminar (¿tiene productos o está cerrado?).'
      );
    }
  }

  async function abrirDetalle(b) {
    try {
      const completo = await obtenerBulto(b.id);
      setDetalle(completo);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'No se pudo cargar el detalle del bulto.'
      );
    }
  }

  function handleActualizarDetalle(actualizado) {
    setDetalle(actualizado);
    cargarBultos();
  }

  async function handleCompletarPacking() {
    setMensajeCompletar('');
    setError('');

    try {
      // Guardamos los datos del pedido antes de limpiar pedidoId
      const pedidoSeleccionado = pedidos.find(
        (p) => p.id === pedidoId
      );

      const respuesta = await completarPacking(pedidoId);

      // Mostramos una confirmación permanente
      setPackingCompletado({
        codigo: pedidoSeleccionado?.codigo || 'Pedido',
        estado: respuesta.estado,
        totalBultos: respuesta.totalBultos
      });

      // Cerramos cualquier detalle abierto
      setDetalle(null);

      // Limpiamos la selección actual
      setPedidoId('');
      setBultos([]);

      // Actualizamos la lista de pedidos pendientes de packing
      const nuevosPedidos = await listarPedidos({
        estado: 'PICKING_COMPLETADO'
      });

      setPedidos(nuevosPedidos);

    } catch (err) {
      setMensajeCompletar(
        err.response?.data?.message ||
        'No se pudo completar el packing.'
      );
    }
  }

  function cerrarConfirmacion() {
    setPackingCompletado(null);
  }

  return (
    <div className="dashboard-layout">
      <Sidebar rol={usuario.rol} />

      <div className="dashboard-content">

        <header className="packing-header">
          <h2>Packing y Bultos</h2>
        </header>

        <main className="packing-main">

          <label className="packing-selector-label">
            Pedido (con picking completado)
          </label>

          <select
            className="packing-selector"
            value={pedidoId}
            onChange={(e) => {
              setPedidoId(e.target.value);
              setPackingCompletado(null);
              setMensajeCompletar('');
              setError('');
            }}
          >
            <option value="">
              Selecciona un pedido
            </option>

            {pedidos.map((p) => (
              <option key={p.id} value={p.id}>
                {p.codigo} — {p.cliente.razonSocial}
              </option>
            ))}
          </select>

          {/* ==============================
              CONFIRMACIÓN DE PACKING
             ============================== */}
          {packingCompletado && (
            <div className="packing-exito">

              <div className="packing-exito-icono">
                ✓
              </div>

              <div className="packing-exito-contenido">

                <h3>
                  Packing completado correctamente
                </h3>

                <p>
                  El proceso de packing del pedido
                  <strong> {packingCompletado.codigo}</strong>
                  {' '}ha finalizado correctamente.
                </p>

                <div className="packing-exito-datos">

                  <div className="packing-exito-dato">
                    <span>Pedido</span>
                    <strong>
                      {packingCompletado.codigo}
                    </strong>
                  </div>

                  <div className="packing-exito-dato">
                    <span>Estado</span>
                    <strong>
                      {packingCompletado.estado}
                    </strong>
                  </div>

                  <div className="packing-exito-dato">
                    <span>Total de bultos</span>
                    <strong>
                      {packingCompletado.totalBultos}
                    </strong>
                  </div>

                </div>

                <p className="packing-exito-siguiente">
                  El pedido puede continuar con el siguiente paso
                  del flujo de despacho.
                </p>

                <button
                  className="packing-exito-btn"
                  onClick={cerrarConfirmacion}
                >
                  Volver a pedidos de packing
                </button>

              </div>
            </div>
          )}

          {/* Mensaje cuando no hay pedido seleccionado */}
          {!pedidoId && !packingCompletado && (
            <p className="packing-vacio">
              Elige un pedido para ver o crear sus bultos.
            </p>
          )}

          {/* ==============================
              BULTOS DEL PEDIDO
             ============================== */}
          {pedidoId && (
            <>
              {puedeGestionar && (
                <button
                  className="packing-nuevo-btn"
                  onClick={handleNuevoBulto}
                >
                  + Nuevo bulto
                </button>
              )}

              {cargandoBultos && (
                <p>Cargando bultos…</p>
              )}

              {error && (
                <p className="packing-error">
                  {error}
                </p>
              )}

              <div className="packing-grid">

                {bultos.map((b) => (
                  <div
                    key={b.id}
                    className="packing-bulto-card"
                    onClick={() => abrirDetalle(b)}
                  >

                    <div className="packing-bulto-header">
                      <strong>{b.codigo}</strong>

                      <span
                        className={`inv-estado-badge bulto-estado-${b.estado}`}
                      >
                        {b.estado}
                      </span>
                    </div>

                    <p>
                      Bulto {b.numero}/{b.totalBultos}
                    </p>

                    <p>
                      {b.peso
                        ? `${b.peso} kg`
                        : 'Sin peso registrado'}
                    </p>

                    {b.estado === 'ABIERTO' && (
                      <button
                        className="packing-bulto-eliminar"
                        onClick={(e) =>
                          handleEliminarBulto(e, b.id)
                        }
                      >
                        Eliminar
                      </button>
                    )}

                  </div>
                ))}

              </div>

              {puedeGestionar && bultos.length > 0 && (
                <div className="packing-completar">

                  <button
                    onClick={handleCompletarPacking}
                  >
                    Completar packing del pedido
                  </button>

                  {mensajeCompletar && (
                    <p className="packing-mensaje packing-mensaje-error">
                      {mensajeCompletar}
                    </p>
                  )}

                </div>
              )}
            </>
          )}

        </main>
      </div>

      {detalle && (
        <BultoDetailModal
          bulto={detalle}
          pedidoId={pedidoId}
          onActualizar={handleActualizarDetalle}
          onCerrar={() => setDetalle(null)}
        />
      )}

    </div>
  );
}
