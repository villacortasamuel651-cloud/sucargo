import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Sidebar from '../components/Sidebar';
import { obtenerResumenDashboard } from '../services/dashboard';
import './Dashboard.css';

const RESUMEN_VACIO = {
  pedidos: 0,
  picking: 0,
  listosDespacho: 0,
  productosStockBajo: 0,
  pedidosRecientes: [],
  alertas: [],
};

// Mapeo de estado de pedido -> clase visual del badge.
// Si tus valores reales de `estado` son distintos a estos, ajusta aquí
// (es la única función nueva que agrego, no toca tu lógica de datos).
function claseEstado(estado) {
  const OK = ['LISTO', 'DESPACHADO', 'STOCK_RESERVADO'];
  const ALERTA = ['PENDIENTE', 'EN_PICKING', 'EN_PACKING'];
  const CRITICO = ['SIN_STOCK', 'CANCELADO'];

  if (OK.includes(estado)) return 'estado-ok';
  if (ALERTA.includes(estado)) return 'estado-alerta';
  if (CRITICO.includes(estado)) return 'estado-critico';
  return '';
}

export default function Dashboard() {
  const { usuario, cerrarSesion } = useAuth();
  const [resumen, setResumen] = useState(RESUMEN_VACIO);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    cargarResumen();
  }, []);

  async function cargarResumen() {
    setCargando(true);
    try {
      const data = await obtenerResumenDashboard();
      setResumen(data);
    } catch (err) {
      // El endpoint de dashboard todavía puede no existir en el backend
      // (pendiente: GET /api/dashboard/resumen). Mientras tanto, mostramos
      // el estado vacío en vez de un error, para no bloquear la pantalla.
      console.warn('No se pudo cargar el resumen del dashboard:', err.message);
      setResumen(RESUMEN_VACIO);
    } finally {
      setCargando(false);
    }
  }

  function formatFecha(fechaISO) {
    return new Date(fechaISO).toLocaleString('es-PE', {
      day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit',
    });
  }

  return (
    <div className="dashboard-layout">
      <Sidebar rol={usuario.rol} />

      <div className="dashboard-content">
        <header className="dashboard-header">
          <p className="dashboard-header-empresa">Distribuidora El Buen Precio S.A.C.</p>
          <div className="dashboard-header-user">
            <div className="dashboard-user-info">
              <span className="dashboard-user-nombre">{usuario.nombre}</span>
              <span className="dashboard-user-rol">{usuario.rol}</span>
            </div>
            <button onClick={cerrarSesion} className="dashboard-logout">Cerrar sesión</button>
          </div>
        </header>

        <main className="dashboard-main">
          <h2>Bienvenido, {usuario.nombre.split(' ')[0]}</h2>
          <p className="dashboard-subtitle">Resumen de hoy</p>

          {/* 1. Resumen de operación */}
          <div className="dashboard-stats">
            <div className="dashboard-stat-card" style={{ '--stat-color': '#4338CA' }}>
              <div className="dashboard-stat-valor">{cargando ? '—' : resumen.pedidos}</div>
              <div className="dashboard-stat-label">Pedidos</div>
            </div>
            <div className="dashboard-stat-card" style={{ '--stat-color': '#22D3EE' }}>
              <div className="dashboard-stat-valor">{cargando ? '—' : resumen.picking}</div>
              <div className="dashboard-stat-label">Picking</div>
            </div>
            <div className="dashboard-stat-card" style={{ '--stat-color': '#1E8E5A' }}>
              <div className="dashboard-stat-valor">{cargando ? '—' : resumen.listosDespacho}</div>
              <div className="dashboard-stat-label">Listos</div>
            </div>
            <div className="dashboard-stat-card" style={{ '--stat-color': '#B23A2E' }}>
              <div className="dashboard-stat-valor">{cargando ? '—' : resumen.productosStockBajo}</div>
              <div className="dashboard-stat-label">Stock bajo</div>
            </div>
          </div>

          <div className="dashboard-grid">
            {/* 2. Pedidos recientes */}
            <section className="dashboard-section">
              <h3>Pedidos recientes</h3>
              {resumen.pedidosRecientes.length === 0 ? (
                <p className="dashboard-vacio">No hay pedidos registrados todavía.</p>
              ) : (
                <table className="dashboard-tabla">
                  <thead>
                    <tr>
                      <th>Código</th><th>Cliente</th><th>Fecha</th><th>Estado</th><th>Prioridad</th>
                    </tr>
                  </thead>
                  <tbody>
                    {resumen.pedidosRecientes.map((p) => (
                      <tr key={p.id}>
                        <td>{p.codigo}</td>
                        <td>{p.cliente}</td>
                        <td>{formatFecha(p.fecha)}</td>
                        <td>
                          <span className={`dashboard-estado-badge ${claseEstado(p.estado)}`}>
                            {p.estado}
                          </span>
                        </td>
                        <td>{p.prioridad}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </section>

            {/* 3. Alertas operativas */}
            <section className="dashboard-section">
              <h3>Alertas operativas</h3>
              {resumen.alertas.length === 0 ? (
                <p className="dashboard-vacio">No hay alertas operativas.</p>
              ) : (
                <ul className="dashboard-alertas">
                  {resumen.alertas.map((a, i) => (
                    <li key={i} className="dashboard-alerta-item">⚠ {a.mensaje}</li>
                  ))}
                </ul>
              )}
            </section>
          </div>

          {/* 4. Acciones rápidas — navegación pura, sin lógica de negocio */}
          <section className="dashboard-section">
            <h3>Acciones rápidas</h3>
            <div className="dashboard-acciones">
              <Link to="/clientes" className="dashboard-accion-btn">+ Nuevo cliente</Link>
              <Link to="/productos" className="dashboard-accion-btn">+ Nuevo producto</Link>
              <Link to="/pedidos" className="dashboard-accion-btn">+ Nuevo pedido</Link>
              <Link to="/inventario" className="dashboard-accion-btn">Ver inventario</Link>
            </div>
          </section>
        </main>
      </div>
    </div>
  );
}