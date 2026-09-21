import { useState } from 'react';
import { verificarBulto, completarDespacho } from '../services/despacho';
import './ClienteDetailModal.css';
import './DespachoDetailModal.css';

export default function DespachoDetailModal({
  despacho,
  onActualizar,
  onCerrar
}) {
  const [codigoEscaneado, setCodigoEscaneado] = useState('');
  const [error, setError] = useState('');
  const [mensaje, setMensaje] = useState('');
  const [procesando, setProcesando] = useState(false);

  const todosVerificados = despacho.bultos.every(
    (b) => b.verificado
  );

  async function handleVerificar(e) {
    e.preventDefault();

    setError('');
    setMensaje('');

    const codigo = codigoEscaneado.trim();

    if (!codigo) {
      return;
    }

    // Buscar el bulto usando el código QR
    const bulto = despacho.bultos.find(
      (b) =>
        b.codigo &&
        b.codigo.trim().toUpperCase() === codigo.toUpperCase()
    );

    if (!bulto) {
      setError(
        'Ese código no corresponde a un bulto de este pedido.'
      );
      return;
    }

    if (bulto.verificado) {
      setError('Este bulto ya fue verificado.');
      return;
    }

    try {
      // El backend necesita el ID del bulto,
      // no el código BUL-XXXXXXXX.
      const actualizado = await verificarBulto(
        despacho.id,
        bulto.id
      );

      onActualizar(actualizado);
      setCodigoEscaneado('');
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'No se pudo verificar el bulto.'
      );
    }
  }

  async function handleCompletar() {
    setProcesando(true);
    setMensaje('');
    setError('');

    try {
      const resultado = await completarDespacho(
        despacho.id
      );

      setMensaje(
        `✅ Pedido despachado. Estado final: ${resultado.pedidoEstado}`
      );

      onActualizar({
        ...despacho,
        estado: resultado.estado
      });
    } catch (err) {
      setMensaje(
        err.response?.data?.message ||
        'No se pudo completar el despacho.'
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
        className="cliente-detail-card despacho-detail-card"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="cliente-detail-header">
          <h2>
            Despacho — {despacho.pedido.codigo}
          </h2>

          <button
            className="cliente-detail-cerrar"
            onClick={onCerrar}
          >
            Cerrar
          </button>
        </div>

        <div className="cliente-detail-tab">

          <label>Orden de transporte</label>

          <p className="cliente-detail-dato">
            {despacho.ordenTransporte}
          </p>

          <label>Estado</label>

          <span
            className={`inv-estado-badge despacho-estado-${despacho.estado}`}
          >
            {despacho.estado}
          </span>

          <label>
            Bultos (
            {
              despacho.bultos.filter(
                (b) => b.verificado
              ).length
            }
            /
            {despacho.bultos.length}
            verificados)
          </label>

          <ul className="despacho-bultos-lista">
            {despacho.bultos.map((b) => (
              <li
                key={b.id}
                className={`despacho-bulto-item ${
                  b.verificado
                    ? 'despacho-bulto-ok'
                    : ''
                }`}
              >
                <span>
                  {b.verificado ? '✓' : '○'}{' '}
                  {b.codigo} — Bulto {b.numero}
                </span>
              </li>
            ))}
          </ul>

          {despacho.estado === 'EN_VERIFICACION' &&
            !todosVerificados && (
              <form
                onSubmit={handleVerificar}
                className="despacho-verificar-form"
              >
                <input
                  placeholder="Pega o escanea el código del bulto…"
                  value={codigoEscaneado}
                  onChange={(e) =>
                    setCodigoEscaneado(
                      e.target.value
                    )
                  }
                  autoFocus
                />

                <button type="submit">
                  Verificar
                </button>
              </form>
            )}

          {error && (
            <p className="modal-error">
              {error}
            </p>
          )}

          {despacho.estado === 'EN_VERIFICACION' &&
            todosVerificados && (
              <button
                className="usuario-btn-primario"
                style={{ marginTop: '1.25rem' }}
                onClick={handleCompletar}
                disabled={procesando}
              >
                {procesando
                  ? 'Despachando…'
                  : 'Completar despacho'}
              </button>
            )}

          {mensaje && (
            <p className="despacho-mensaje">
              {mensaje}
            </p>
          )}
        </div>
      </div>
    </div>
  );
}