import { useState, useEffect, useMemo } from 'react';
import Sidebar from '../components/Sidebar';
import ClienteCard from '../components/ClienteCard';
import ClienteModal from '../components/ClienteModal';
import ClienteDetailModal from '../components/ClienteDetailModal';
import { useAuth } from '../context/AuthContext';
import {
  listarClientes,
  obtenerCliente,
  crearCliente,
  editarCliente,
  desactivarCliente,
} from '../services/clientes';
import './Clientes.css';

const COLORES_AVATAR = ['#4338CA', '#B0770B', '#1E8E5A', '#B23A2E', '#0F7C8A'];

function colorAvatar(texto) {
  let hash = 0;
  for (let i = 0; i < texto.length; i++) hash = texto.charCodeAt(i) + ((hash << 5) - hash);
  return COLORES_AVATAR[Math.abs(hash) % COLORES_AVATAR.length];
}

export default function Clientes() {
  const { usuario } = useAuth();
  const [clientes, setClientes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [modalAbierto, setModalAbierto] = useState(false);
  const [clienteEditando, setClienteEditando] = useState(null);
  const [clienteDetalle, setClienteDetalle] = useState(null);

  const [busqueda, setBusqueda] = useState('');
  const [filtroEstado, setFiltroEstado] = useState('TODOS');

  useEffect(() => {
    cargarClientes();
  }, []);

  async function cargarClientes() {
    setCargando(true);
    setError('');
    try {
      const data = await listarClientes();
      setClientes(data);
    } catch (err) {
      setError('No se pudo conectar con el servidor.');
    } finally {
      setCargando(false);
    }
  }

  function abrirNuevo() {
    setClienteEditando(null);
    setModalAbierto(true);
  }

  function abrirEditar(cliente) {
    setClienteEditando(cliente);
    setModalAbierto(true);
  }

  async function abrirDetalle(cliente) {
    const detalle = await obtenerCliente(cliente.id);
    setClienteDetalle(detalle);
  }

  function handleActualizarDetalle(actualizado) {
    setClienteDetalle(actualizado);
    cargarClientes(); // refresca el contador de puntos de entrega en la tarjeta
  }

  async function handleGuardar(form, id) {
    if (id) {
      await editarCliente(id, form);
    } else {
      await crearCliente(form);
    }
    setModalAbierto(false);
    cargarClientes();
  }

  async function handleDesactivar(cliente) {
    if (!confirm(`¿Desactivar a ${cliente.razonSocial}?`)) return;
    await desactivarCliente(cliente.id);
    cargarClientes();
  }

  // ---- Stats de resumen (derivadas de los mismos datos, sin llamadas extra) ----
  const stats = useMemo(() => {
    const total = clientes.length;
    const activos = clientes.filter((c) => c.estado !== 'INACTIVO').length;
    const puntosEntrega = clientes.reduce((acc, c) => acc + (c.cantidadPuntosEntrega ?? 0), 0);
    return { total, activos, puntosEntrega };
  }, [clientes]);

  // ---- Filtro de búsqueda + estado, client-side ----
  const clientesFiltrados = useMemo(() => {
    return clientes.filter((c) => {
      const coincideTexto =
        c.razonSocial.toLowerCase().includes(busqueda.toLowerCase()) ||
        c.rucDni.includes(busqueda);
      const coincideEstado =
        filtroEstado === 'TODOS' ||
        (filtroEstado === 'ACTIVO' && c.estado !== 'INACTIVO') ||
        (filtroEstado === 'INACTIVO' && c.estado === 'INACTIVO');
      return coincideTexto && coincideEstado;
    });
  }, [clientes, busqueda, filtroEstado]);

  return (
    <div className="dashboard-layout">
      <Sidebar rol={usuario.rol} />

      <div className="dashboard-content">
        <header className="clientes-header">
          <h2>Clientes</h2>
          <button className="clientes-nuevo-btn" onClick={abrirNuevo}>+ Nuevo cliente</button>
        </header>

        <main className="clientes-main">
          {cargando && <p>Cargando clientes…</p>}
          {error && <p className="clientes-error">{error}</p>}
          {!cargando && !error && clientes.length === 0 && (
            <p className="clientes-vacio">Todavía no hay clientes registrados.</p>
          )}

          {!cargando && !error && clientes.length > 0 && (
            <>
              {/* ---- Tarjetas de resumen ---- */}
              <div className="clientes-stats">
                <div className="stat-card">
                  <span className="stat-label">Total clientes</span>
                  <span className="stat-value">{stats.total}</span>
                </div>
                <div className="stat-card">
                  <span className="stat-label">Activos</span>
                  <span className="stat-value stat-value-green">{stats.activos}</span>
                </div>
                <div className="stat-card">
                  <span className="stat-label">Puntos de entrega</span>
                  <span className="stat-value">{stats.puntosEntrega}</span>
                </div>
              </div>

              {/* ---- Buscador + filtro ---- */}
              <div className="clientes-toolbar">
                <input
                  className="clientes-buscador"
                  type="text"
                  placeholder="Buscar por nombre o RUC/DNI…"
                  value={busqueda}
                  onChange={(e) => setBusqueda(e.target.value)}
                />
                <div className="clientes-filtros">
                  {['TODOS', 'ACTIVO', 'INACTIVO'].map((f) => (
                    <button
                      key={f}
                      className={`filtro-chip ${filtroEstado === f ? 'filtro-chip-activo' : ''}`}
                      onClick={() => setFiltroEstado(f)}
                    >
                      {f === 'TODOS' ? 'Todos' : f === 'ACTIVO' ? 'Activos' : 'Inactivos'}
                    </button>
                  ))}
                </div>
              </div>

              {clientesFiltrados.length === 0 && (
                <p className="clientes-vacio">No se encontraron clientes con ese criterio.</p>
              )}

              {clientesFiltrados.length > 0 && (
                <>
                  {/* ---- Vista tabla: visible desde 861px (ver Clientes.css) ---- */}
                  <div className="clientes-table-wrap">
                    <table className="clientes-table">
                      <thead>
                        <tr>
                          <th>Cliente</th>
                          <th>RUC / DNI</th>
                          <th>Teléfono</th>
                          <th>Puntos de entrega</th>
                          <th>Estado</th>
                          <th></th>
                        </tr>
                      </thead>
                      <tbody>
                        {clientesFiltrados.map((c) => {
                          const inactivo = c.estado === 'INACTIVO';
                          const inicial = c.razonSocial.trim().charAt(0).toUpperCase();
                          return (
                            <tr key={c.id} className={inactivo ? 'fila-inactiva' : ''}>
                              <td>
                                <div className="col-cliente">
                                  <span
                                    className="avatar-inicial"
                                    style={{ background: colorAvatar(c.razonSocial) }}
                                  >
                                    {inicial}
                                  </span>
                                  <span className="col-nombre">{c.razonSocial}</span>
                                </div>
                              </td>
                              <td className="col-muted">{c.rucDni}</td>
                              <td className="col-muted">{c.telefono || '—'}</td>
                              <td className="col-muted">{c.cantidadPuntosEntrega ?? 0}</td>
                              <td>
                                <span className={`tabla-badge ${inactivo ? 'tabla-badge-inactivo' : ''}`}>
                                  {c.estado}
                                </span>
                              </td>
                              <td>
                                <div className="tabla-acciones">
                                  <button
                                    className="icon-btn"
                                    title="Puntos de entrega"
                                    onClick={() => abrirDetalle(c)}
                                  >
                                    📍
                                  </button>
                                  <button
                                    className="icon-btn"
                                    title="Editar"
                                    onClick={() => abrirEditar(c)}
                                  >
                                    ✎
                                  </button>
                                  {!inactivo && (
                                    <button
                                      className="icon-btn icon-btn-danger"
                                      title="Desactivar"
                                      onClick={() => handleDesactivar(c)}
                                    >
                                      ⏻
                                    </button>
                                  )}
                                </div>
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>

                  {/* ---- Vista cards: visible hasta 860px (ver Clientes.css) ---- */}
                  <div className="clientes-grid">
                    {clientesFiltrados.map((c) => (
                      <ClienteCard
                        key={c.id}
                        cliente={c}
                        onEditar={abrirEditar}
                        onDesactivar={handleDesactivar}
                        onVerDetalle={abrirDetalle}
                      />
                    ))}
                  </div>
                </>
              )}
            </>
          )}
        </main>
      </div>

      {modalAbierto && (
        <ClienteModal cliente={clienteEditando} onGuardar={handleGuardar} onCerrar={() => setModalAbierto(false)} />
      )}

      {clienteDetalle && (
        <ClienteDetailModal
          cliente={clienteDetalle}
          onActualizar={handleActualizarDetalle}
          onCerrar={() => setClienteDetalle(null)}
        />
      )}
    </div>
  );
}