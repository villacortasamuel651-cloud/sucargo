import { useState, useEffect } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import {
  obtenerProductosDelPedido,
  agregarProducto,
  quitarProducto,
  registrarPeso,
  cerrarBulto
} from '../services/bultos';

import './ClienteDetailModal.css';
import './BultoDetailModal.css';

export default function BultoDetailModal({
  bulto,
  pedidoId,
  onActualizar,
  onCerrar
}) {
  const [pendientes, setPendientes] = useState([]);
  const [productoId, setProductoId] = useState('');
  const [cantidad, setCantidad] = useState('');
  const [peso, setPeso] = useState(bulto.peso ?? '');
  const [error, setError] = useState('');
  const [procesando, setProcesando] = useState(false);

  const editable = bulto.estado === 'ABIERTO';

  useEffect(() => {
    if (editable) cargarPendientes();
  }, [bulto.productos.length, editable, pedidoId]);

  async function cargarPendientes() {
    try {
      const data = await obtenerProductosDelPedido(pedidoId);
      setPendientes(data.filter((p) => p.cantidadPendiente > 0));
    } catch (err) {
      console.error('Error cargando productos del pedido:', err);
      setPendientes([]);
    }
  }

  async function handleAgregarProducto(e) {
    e.preventDefault();
    setError('');

    if (!productoId || Number(cantidad) <= 0) {
      setError('Selecciona un producto y una cantidad válida.');
      return;
    }

    const producto = pendientes.find((p) => p.productoId === productoId);

    if (!producto) {
      setError('El producto seleccionado ya no tiene unidades pendientes.');
      return;
    }

    if (Number(cantidad) > producto.cantidadPendiente) {
      setError(`Solo puedes agregar hasta ${producto.cantidadPendiente} unidades pendientes.`);
      return;
    }

    try {
      const cantidadAgregar = Number(cantidad);
      const nuevoProducto = await agregarProducto(bulto.id, {
        productoId,
        cantidad: cantidadAgregar
      });

      onActualizar({
        ...bulto,
        productos: [
          ...bulto.productos,
          {
            ...nuevoProducto,
            productoId,
            sku: producto.sku,
            nombre: producto.nombre,
            cantidad: cantidadAgregar
          }
        ]
      });

      setProductoId('');
      setCantidad('');
      await cargarPendientes();
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'No se pudo agregar el producto.'
      );
    }
  }

  function handleSeleccionarProducto(id) {
    setProductoId(id);

    const producto = pendientes.find((p) => p.productoId === id);
    setCantidad(producto ? String(producto.cantidadPendiente) : '');
  }

  async function handleQuitar(detalleId) {
    setError('');

    try {
      await quitarProducto(bulto.id, detalleId);

      onActualizar({
        ...bulto,
        productos: bulto.productos.filter((p) => p.id !== detalleId)
      });

      await cargarPendientes();
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'No se pudo quitar el producto.'
      );
    }
  }

  async function handleGuardarPeso() {
    if (!peso || Number(peso) <= 0) {
      setError('Ingresa un peso válido mayor que cero.');
      return;
    }

    setProcesando(true);
    setError('');

    try {
      const actualizado = await registrarPeso(bulto.id, Number(peso));

      onActualizar({
        ...bulto,
        ...actualizado
      });
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'No se pudo guardar el peso.'
      );
    } finally {
      setProcesando(false);
    }
  }

  async function handleCerrar() {
    if (!confirm('¿Cerrar este bulto? Ya no podrás modificar sus productos.')) {
      return;
    }

    setProcesando(true);
    setError('');

    try {
      const actualizado = await cerrarBulto(bulto.id);

      onActualizar({
        ...bulto,
        ...actualizado
      });
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'No se pudo cerrar el bulto.'
      );
    } finally {
      setProcesando(false);
    }
  }

  function handleImprimir() {
    const ventana = window.open('', '_blank', 'width=800,height=900');

    if (!ventana) {
      setError('No se pudo abrir la ventana de impresión. Revisa el bloqueo de ventanas emergentes.');
      return;
    }

    const productosHTML = bulto.productos.length > 0
      ? bulto.productos.map((p) => `
          <tr>
            <td>${p.sku || '-'}</td>
            <td>${p.nombre || '-'}</td>
            <td>${p.cantidad}</td>
          </tr>
        `).join('')
      : `
          <tr>
            <td colspan="3">Sin productos registrados</td>
          </tr>
        `;

    ventana.document.write(`
      <!DOCTYPE html>
      <html lang="es">
      <head>
        <meta charset="UTF-8">
        <title>Etiqueta ${bulto.codigo}</title>
        <style>
          * { box-sizing: border-box; }

          body {
            margin: 0;
            padding: 20px;
            font-family: Arial, sans-serif;
            color: #1c2128;
            background: #fff;
          }

          .etiqueta {
            width: 100%;
            max-width: 600px;
            margin: 0 auto;
            border: 2px solid #1c2128;
            border-radius: 12px;
            padding: 24px;
          }

          .encabezado {
            text-align: center;
            border-bottom: 1px solid #d7dae1;
            padding-bottom: 16px;
            margin-bottom: 20px;
          }

          .marca {
            font-size: 22px;
            font-weight: 700;
            margin-bottom: 6px;
          }

          .titulo {
            font-size: 14px;
            color: #6b7280;
          }

          .qr {
            text-align: center;
            margin: 10px 0 18px;
          }

          .codigo {
            text-align: center;
            font-size: 22px;
            font-weight: 700;
            letter-spacing: 1px;
            margin-bottom: 18px;
          }

          .datos {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 10px;
            margin-bottom: 20px;
          }

          .dato {
            border: 1px solid #d7dae1;
            border-radius: 8px;
            padding: 10px;
          }

          .dato-label {
            display: block;
            font-size: 11px;
            color: #6b7280;
            margin-bottom: 4px;
          }

          .dato-valor {
            font-size: 14px;
            font-weight: 600;
          }

          h3 {
            font-size: 14px;
            margin: 0 0 10px;
          }

          table {
            width: 100%;
            border-collapse: collapse;
            font-size: 12px;
          }

          th, td {
            border: 1px solid #d7dae1;
            padding: 8px;
            text-align: left;
          }

          th {
            background: #f4f6fb;
          }

          .pie {
            margin-top: 20px;
            padding-top: 12px;
            border-top: 1px solid #d7dae1;
            text-align: center;
            font-size: 10px;
            color: #6b7280;
          }

          @media print {
            body {
              padding: 0;
            }

            .etiqueta {
              border: 2px solid #000;
              max-width: none;
            }
          }
        </style>
      </head>

      <body>
        <div class="etiqueta">

          <div class="encabezado">
            <div class="marca">SUCARGO</div>
            <div class="titulo">Etiqueta de bulto</div>
          </div>

          <div class="qr">
            ${document.querySelector('.bulto-qr-panel svg')?.outerHTML || ''}
          </div>

          <div class="codigo">
            ${bulto.codigo}
          </div>

          <div class="datos">
            <div class="dato">
              <span class="dato-label">Pedido</span>
              <span class="dato-valor">${pedidoId}</span>
            </div>

            <div class="dato">
              <span class="dato-label">Bulto</span>
              <span class="dato-valor">${bulto.numero} / ${bulto.totalBultos}</span>
            </div>

            <div class="dato">
              <span class="dato-label">Estado</span>
              <span class="dato-valor">${bulto.estado}</span>
            </div>

            <div class="dato">
              <span class="dato-label">Peso</span>
              <span class="dato-valor">${bulto.peso ?? '-'} kg</span>
            </div>
          </div>

          <h3>Productos</h3>

          <table>
            <thead>
              <tr>
                <th>SKU</th>
                <th>Producto</th>
                <th>Cantidad</th>
              </tr>
            </thead>

            <tbody>
              ${productosHTML}
            </tbody>
          </table>

          <div class="pie">
            Identificación interna de SUCargo · ${bulto.codigo}
          </div>

        </div>

        <script>
          window.onload = function() {
            window.print();
          };
        </script>
      </body>
      </html>
    `);

    ventana.document.close();
  }

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div
        className="cliente-detail-card bulto-detail-card"
        onClick={(e) => e.stopPropagation()}
      >

        {/* HEADER */}
        <div className="cliente-detail-header">
          <h2>
            {bulto.codigo} — Bulto {bulto.numero}/{bulto.totalBultos}
          </h2>

          <button
            className="cliente-detail-cerrar"
            onClick={onCerrar}
          >
            Cerrar ventana
          </button>
        </div>

        <div className="bulto-detail-layout">

          {/* QR */}
          <div className="bulto-qr-panel">
            <QRCodeSVG value={bulto.id} size={140} />

            <span
              className={`inv-estado-badge bulto-estado-${bulto.estado}`}
            >
              {bulto.estado}
            </span>

            <button
              type="button"
              className="bulto-imprimir-btn"
              onClick={handleImprimir}
            >
              🖨 Imprimir etiqueta
            </button>
          </div>

          <div className="cliente-detail-tab">

            {/* PESO */}
            <label>Peso real del bulto (kg)</label>

            <div className="bulto-peso-row">
              <input
                type="number"
                step="0.01"
                min="0"
                value={peso}
                onChange={(e) => setPeso(e.target.value)}
                disabled={!editable}
              />

              {editable && (
                <button
                  className="picking-guardar-linea"
                  onClick={handleGuardarPeso}
                  disabled={procesando}
                >
                  Guardar
                </button>
              )}
            </div>

            {/* PRODUCTOS DEL BULTO */}
            <label>Productos en este bulto</label>

            <ul className="bulto-productos-lista">
              {bulto.productos.length === 0 && (
                <p className="cliente-detail-vacio">
                  Sin productos todavía.
                </p>
              )}

              {bulto.productos.map((p) => (
                <li
                  key={p.id || p.productoId}
                  className="bulto-producto-item"
                >
                  <span>
                    {p.nombre} ({p.sku}) — {p.cantidad}
                  </span>

                  {editable && p.id && (
                    <button onClick={() => handleQuitar(p.id)}>
                      Quitar
                    </button>
                  )}
                </li>
              ))}
            </ul>

            {/* PRODUCTOS PENDIENTES */}
            {editable && (
              <>
                <label>Productos pendientes de empaquetar</label>

                {pendientes.length === 0 ? (
                  <p
                    className="cliente-detail-vacio"
                    style={{ margin: '0.5rem 0' }}
                  >
                    Ya se distribuyó todo lo recolectado de este pedido.
                  </p>
                ) : (
                  <form
                    onSubmit={handleAgregarProducto}
                    className="bulto-agregar-form"
                  >
                    <select
                      value={productoId}
                      onChange={(e) =>
                        handleSeleccionarProducto(e.target.value)
                      }
                    >
                      <option value="">Producto pendiente…</option>

                      {pendientes.map((p) => (
                        <option
                          key={p.productoId}
                          value={p.productoId}
                        >
                          {p.sku} — {p.nombre} (
                          recolectado: {p.cantidadRecolectada} |
                          pendiente: {p.cantidadPendiente})
                        </option>
                      ))}
                    </select>

                    <input
                      type="number"
                      min="1"
                      max={
                        pendientes.find(
                          (p) => p.productoId === productoId
                        )?.cantidadPendiente || 1
                      }
                      placeholder="Cant."
                      value={cantidad}
                      onChange={(e) => setCantidad(e.target.value)}
                    />

                    <button type="submit">
                      + Agregar
                    </button>
                  </form>
                )}
              </>
            )}

            {/* ERROR */}
            {error && (
              <div className="bulto-error">
                <span>⚠</span>
                <p>{error}</p>
              </div>
            )}

            {/* CERRAR BULTO */}
            {editable && (
              <button
                className="usuario-btn-primario"
                style={{ marginTop: '1.25rem' }}
                onClick={handleCerrar}
                disabled={procesando}
              >
                {procesando ? 'Cerrando…' : 'Cerrar bulto'}
              </button>
            )}

          </div>
        </div>
      </div>
    </div>
  );
}