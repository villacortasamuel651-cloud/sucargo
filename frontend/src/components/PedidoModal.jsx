import { useState, useEffect } from 'react';
import { listarClientes, obtenerCliente } from '../services/clientes';
import { listarProductos } from '../services/productos';
import {
  crearPedido,
  obtenerAlmacenesDisponibles,
} from '../services/pedidos';
import './ClienteModal.css';
import './PedidoModal.css';

const PRIORIDADES = ['BAJA', 'MEDIA', 'ALTA'];

export default function PedidoModal({ onGuardar, onCerrar }) {
  const [clientes, setClientes] = useState([]);
  const [productos, setProductos] = useState([]);
  const [puntosEntrega, setPuntosEntrega] = useState([]);
  const [almacenes, setAlmacenes] = useState([]);

  const [clienteId, setClienteId] = useState('');
  const [puntoEntregaId, setPuntoEntregaId] = useState('');
  const [almacenId, setAlmacenId] = useState('');
  const [prioridad, setPrioridad] = useState('MEDIA');
  const [items, setItems] = useState([
    { productoId: '', cantidad: '' },
  ]);

  const [buscandoAlmacenes, setBuscandoAlmacenes] = useState(false);
  const [almacenesBuscados, setAlmacenesBuscados] = useState(false);

  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    listarClientes()
      .then(setClientes)
      .catch(() => setClientes([]));

    listarProductos()
      .then(setProductos)
      .catch(() => setProductos([]));
  }, []);

  useEffect(() => {
    if (!clienteId) {
      setPuntosEntrega([]);
      setPuntoEntregaId('');
      return;
    }

    obtenerCliente(clienteId)
      .then((c) => {
        setPuntosEntrega(c.puntosEntrega || []);
        setPuntoEntregaId('');
      });
  }, [clienteId]);

  function actualizarItem(index, campo, valor) {
    const nuevos = [...items];

    nuevos[index] = {
      ...nuevos[index],
      [campo]: valor,
    };

    setItems(nuevos);

    // Si cambia el pedido, la búsqueda anterior deja de ser válida.
    setAlmacenesBuscados(false);
    setAlmacenes([]);
    setAlmacenId('');
  }

  function agregarLinea() {
    setItems([
      ...items,
      { productoId: '', cantidad: '' },
    ]);

    setAlmacenesBuscados(false);
    setAlmacenes([]);
    setAlmacenId('');
  }

  function quitarLinea(index) {
    const nuevosItems = items.filter((_, i) => i !== index);

    setItems(nuevosItems);

    setAlmacenesBuscados(false);
    setAlmacenes([]);
    setAlmacenId('');
  }

  async function buscarAlmacenes() {
    setError('');
    setAlmacenId('');

    const itemsValidos = items.filter(
      (i) =>
        i.productoId &&
        Number(i.cantidad) > 0
    );

    if (itemsValidos.length === 0) {
      setError(
        'Agrega al menos un producto con cantidad válida.'
      );
      return;
    }

    setBuscandoAlmacenes(true);

    try {
      const data = await obtenerAlmacenesDisponibles(
        itemsValidos.map((i) => ({
          productoId: i.productoId,
          cantidad: Number(i.cantidad),
        }))
      );

      setAlmacenes(data);
      setAlmacenesBuscados(true);

      if (data.length === 0) {
        setError(
          'Ningún almacén tiene stock suficiente para todo el pedido.'
        );
      }
    } catch (err) {
      setAlmacenes([]);
      setAlmacenesBuscados(false);

      setError(
        err.response?.data?.message ||
        'No se pudieron consultar los almacenes disponibles.'
      );
    } finally {
      setBuscandoAlmacenes(false);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (!clienteId || !puntoEntregaId) {
      setError(
        'Cliente y punto de entrega son obligatorios.'
      );
      return;
    }

    if (!almacenId) {
      setError(
        'Selecciona un almacén antes de registrar el pedido.'
      );
      return;
    }

    const itemsValidos = items.filter(
      (i) =>
        i.productoId &&
        Number(i.cantidad) > 0
    );

    if (itemsValidos.length === 0) {
      setError(
        'Agrega al menos un producto con cantidad válida.'
      );
      return;
    }

    setGuardando(true);

    try {
      const pedido = await crearPedido({
        clienteId,
        puntoEntregaId,
        almacenId,
        prioridad,
        items: itemsValidos.map((i) => ({
          productoId: i.productoId,
          cantidad: Number(i.cantidad),
        })),
      });

      onGuardar(pedido);

    } catch (err) {
      setError(
        err.response?.data?.message ||
        'No se pudo registrar el pedido.'
      );
    } finally {
      setGuardando(false);
    }
  }

  return (
    <div
      className="modal-overlay"
      onClick={onCerrar}
    >
      <div
        className="modal-card pedido-modal-card"
        onClick={(e) => e.stopPropagation()}
      >
        <h2>Nuevo pedido</h2>

        <form
          onSubmit={handleSubmit}
          className="modal-form"
        >
          <label>Cliente</label>

          <select
            value={clienteId}
            onChange={(e) =>
              setClienteId(e.target.value)
            }
          >
            <option value="">
              Selecciona un cliente
            </option>

            {clientes.map((c) => (
              <option
                key={c.id}
                value={c.id}
              >
                {c.razonSocial}
              </option>
            ))}
          </select>

          <label>Punto de entrega</label>

          <select
            value={puntoEntregaId}
            onChange={(e) =>
              setPuntoEntregaId(e.target.value)
            }
            disabled={!clienteId}
          >
            <option value="">
              {clienteId
                ? 'Selecciona un punto de entrega'
                : 'Elige primero un cliente'}
            </option>

            {puntosEntrega.map((p) => (
              <option
                key={p.id}
                value={p.id}
              >
                {p.direccion}
                {p.distrito
                  ? ` — ${p.distrito}`
                  : ''}
              </option>
            ))}
          </select>

          <label>Prioridad</label>

          <select
            value={prioridad}
            onChange={(e) =>
              setPrioridad(e.target.value)
            }
          >
            {PRIORIDADES.map((p) => (
              <option
                key={p}
                value={p}
              >
                {p}
              </option>
            ))}
          </select>

          <label>Productos</label>

          <div className="pedido-items">
            {items.map((item, i) => (
              <div
                key={i}
                className="pedido-item-linea"
              >
                <select
                  value={item.productoId}
                  onChange={(e) =>
                    actualizarItem(
                      i,
                      'productoId',
                      e.target.value
                    )
                  }
                >
                  <option value="">
                    Producto…
                  </option>

                  {productos.map((p) => (
                    <option
                      key={p.id}
                      value={p.id}
                    >
                      {p.sku} — {p.nombre}
                    </option>
                  ))}
                </select>

                <input
                  type="number"
                  min="1"
                  placeholder="Cant."
                  value={item.cantidad}
                  onChange={(e) =>
                    actualizarItem(
                      i,
                      'cantidad',
                      e.target.value
                    )
                  }
                />

                {items.length > 1 && (
                  <button
                    type="button"
                    className="pedido-item-quitar"
                    onClick={() =>
                      quitarLinea(i)
                    }
                  >
                    ✕
                  </button>
                )}
              </div>
            ))}
          </div>

          <button
            type="button"
            className="pedido-agregar-linea"
            onClick={agregarLinea}
          >
            + Agregar producto
          </button>

          <button
            type="button"
            onClick={buscarAlmacenes}
            disabled={buscandoAlmacenes}
          >
            {buscandoAlmacenes
              ? 'Buscando almacenes…'
              : 'Buscar almacenes disponibles'}
          </button>

          {almacenesBuscados &&
            almacenes.length > 0 && (
              <>
                <label>Almacén</label>

                <select
                  value={almacenId}
                  onChange={(e) =>
                    setAlmacenId(
                      e.target.value
                    )
                  }
                >
                  <option value="">
                    Selecciona un almacén
                  </option>

                  {almacenes.map((almacen) => (
                    <option
                      key={almacen.id}
                      value={almacen.id}
                    >
                      {almacen.nombre}
                    </option>
                  ))}
                </select>
              </>
            )}

          {almacenesBuscados &&
            almacenes.length === 0 &&
            !error && (
              <p className="modal-error">
                Ningún almacén puede atender
                todo el pedido.
              </p>
            )}

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
              disabled={
                guardando ||
                !almacenId
              }
            >
              {guardando
                ? 'Registrando…'
                : 'Registrar pedido'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}