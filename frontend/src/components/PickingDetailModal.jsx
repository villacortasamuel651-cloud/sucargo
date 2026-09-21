import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  iniciarPicking,
  registrarLinea,
  completarPicking
} from '../services/picking';
import './ClienteDetailModal.css';
import './PickingDetailModal.css';

const ROLES_EJECUTORES = [
  'ALMACENERO',
  'SUPERVISOR_ALMACEN'
];

export default function PickingDetailModal({
  picking,
  onActualizar,
  onCerrar
}) {
  const { usuario } = useAuth();

  const puedeEjecutar = ROLES_EJECUTORES.includes(
    usuario.rol
  );

  const [cantidades, setCantidades] = useState(
    Object.fromEntries(
      picking.lineas.map((l) => [
        l.id,
        l.cantidadRecolectada || ''
      ])
    )
  );

  const [incidencias, setIncidencias] = useState(
    Object.fromEntries(
      picking.lineas.map((l) => [
        l.id,
        l.incidencia || ''
      ])
    )
  );

  const [procesando, setProcesando] = useState(false);
  const [error, setError] = useState('');

  async function handleIniciar() {
    setProcesando(true);
    setError('');

    try {
      const actualizado = await iniciarPicking(
        picking.id
      );

      onActualizar({
        ...picking,
        ...actualizado
      });

    } catch (err) {
      setError(
        err.response?.data?.message ||
        'No se pudo iniciar el picking.'
      );
    } finally {
      setProcesando(false);
    }
  }

  async function handleGuardarLinea(lineaId) {
    setError('');

    try {
      await registrarLinea(
        picking.id,
        lineaId,
        {
          cantidadRecolectada:
            Number(cantidades[lineaId]) || 0,
          incidencia:
            incidencias[lineaId] || null
        }
      );

      onActualizar({
        ...picking,
        lineas: picking.lineas.map((l) =>
          l.id === lineaId
            ? {
                ...l,
                cantidadRecolectada:
                  Number(cantidades[lineaId]) || 0,
                incidencia:
                  incidencias[lineaId] || null
              }
            : l
        )
      });

    } catch (err) {
      setError(
        err.response?.data?.message ||
        'No se pudo guardar la línea.'
      );
    }
  }

  async function handleCompletar() {
    setProcesando(true);
    setError('');

    try {
      const resultado =
        await completarPicking(picking.id);

      onActualizar({
        ...picking,
        estado: resultado.estado
      });

    } catch (err) {
      setError(
        err.response?.data?.message ||
        'No se pudo completar el picking. ¿Faltan líneas por registrar?'
      );
    } finally {
      setProcesando(false);
    }
  }

  return (
    <div
      className="modal-overlay"
      onClick={onCerrar}
    >
      <div
        className="cliente-detail-card picking-detail-card"
        onClick={(e) => e.stopPropagation()}
      >

        <div className="cliente-detail-header">
          <h2>
            Picking — {picking.pedido.codigo}
          </h2>

          <button
            className="cliente-detail-cerrar"
            onClick={onCerrar}
          >
            Cerrar
          </button>
        </div>

        <div className="cliente-detail-tab">

          <label>Estado</label>

          <span
            className={`inv-estado-badge picking-estado-${picking.estado}`}
          >
            {picking.estado}
          </span>

          {picking.usuario && (
            <>
              <label>Asignado a</label>

              <p className="cliente-detail-dato">
                {picking.usuario}
              </p>
            </>
          )}

          {picking.estado === 'PENDIENTE' &&
            puedeEjecutar && (
              <button
                className="usuario-btn-primario"
                onClick={handleIniciar}
                disabled={procesando}
              >
                {procesando
                  ? 'Iniciando…'
                  : 'Iniciar picking'}
              </button>
          )}

          <label
            style={{
              marginTop: '1.25rem'
            }}
          >
            Líneas a recolectar
          </label>

          <table className="picking-lineas-tabla">

            <thead>
              <tr>
                <th>Producto</th>
                <th>Esperado</th>
                <th>Recolectado</th>
                <th>Incidencia</th>

                {puedeEjecutar && (
                  <th></th>
                )}
              </tr>
            </thead>

            <tbody>
              {picking.lineas.map((l) => (
                <tr key={l.id}>

                  <td>
                    {l.producto.nombre}
                  </td>

                  <td>
                    {l.cantidadEsperada}
                  </td>

                  <td>
                    {puedeEjecutar &&
                    picking.estado === 'EN_PROCESO' ? (
                      <input
                        type="number"
                        min="0"
                        value={
                          cantidades[l.id]
                        }
                        onChange={(e) =>
                          setCantidades({
                            ...cantidades,
                            [l.id]:
                              e.target.value
                          })
                        }
                      />
                    ) : (
                      l.cantidadRecolectada
                    )}
                  </td>

                  <td>
                    {puedeEjecutar &&
                    picking.estado === 'EN_PROCESO' ? (
                      <input
                        placeholder="Opcional"
                        value={
                          incidencias[l.id]
                        }
                        onChange={(e) =>
                          setIncidencias({
                            ...incidencias,
                            [l.id]:
                              e.target.value
                          })
                        }
                      />
                    ) : (
                      l.incidencia || '—'
                    )}
                  </td>

                  {puedeEjecutar &&
                    picking.estado === 'EN_PROCESO' && (
                      <td>
                        <button
                          className="picking-guardar-linea"
                          onClick={() =>
                            handleGuardarLinea(
                              l.id
                            )
                          }
                        >
                          Guardar
                        </button>
                      </td>
                    )}

                </tr>
              ))}
            </tbody>

          </table>

          {error && (
            <p className="modal-error">
              {error}
            </p>
          )}

          {picking.estado === 'EN_PROCESO' &&
            puedeEjecutar && (
              <button
                className="usuario-btn-primario"
                style={{
                  marginTop: '1.25rem'
                }}
                onClick={handleCompletar}
                disabled={procesando}
              >
                {procesando
                  ? 'Completando…'
                  : 'Completar picking'}
              </button>
            )}

        </div>
      </div>
    </div>
  );
}

