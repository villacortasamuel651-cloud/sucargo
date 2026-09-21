import { Link, useLocation } from 'react-router-dom';
import './Sidebar.css';

// Módulos del sistema, su ruta y qué roles pueden verlos, según la Matriz
// de Roles y Permisos del documento de proyecto.
const MODULOS = [
  { nombre: 'Dashboard', ruta: '/dashboard', roles: ['ADMIN_EMPRESA', 'VENTAS', 'ALMACENERO', 'SUPERVISOR_ALMACEN', 'LOGISTICA', 'DESPACHO'] },
  { nombre: 'Clientes', ruta: '/clientes', roles: ['ADMIN_EMPRESA', 'VENTAS'] },
  { nombre: 'Productos', ruta: '/productos', roles: ['ADMIN_EMPRESA'] },
  { nombre: 'Almacenes', ruta: '/almacenes', roles: ['ADMIN_EMPRESA'] },
  { nombre: 'Inventario', ruta: '/inventario', roles: ['ADMIN_EMPRESA', 'ALMACENERO'] },
  { nombre: 'Pedidos', ruta: '/pedidos', roles: ['ADMIN_EMPRESA', 'VENTAS', 'ALMACENERO', 'SUPERVISOR_ALMACEN', 'LOGISTICA', 'DESPACHO'] },
  { nombre: 'Picking', ruta: '/picking', roles: ['ADMIN_EMPRESA', 'VENTAS', 'ALMACENERO', 'SUPERVISOR_ALMACEN', 'LOGISTICA', 'DESPACHO'] },
  { nombre: 'Packing', ruta: '/packing', roles: ['ADMIN_EMPRESA', 'VENTAS', 'ALMACENERO', 'SUPERVISOR_ALMACEN', 'LOGISTICA', 'DESPACHO'] },
  { nombre: 'Transportistas', ruta: '/transportistas', roles: ['ADMIN_EMPRESA', 'LOGISTICA', 'DESPACHO'] },
  { nombre: 'Distribución', ruta: '/distribucion', roles: ['ADMIN_EMPRESA', 'LOGISTICA', 'DESPACHO'] },
  { nombre: 'Órdenes de Transporte', ruta: '/ordenes-transporte', roles: ['ADMIN_EMPRESA', 'LOGISTICA', 'DESPACHO'] },
  { nombre: 'Despachos', ruta: '/despacho', roles: ['DESPACHO'] },
  { nombre: 'Usuarios', ruta: '/usuarios', roles: ['ADMIN_EMPRESA'] },
  { nombre: 'Empresas', ruta: '/empresas', roles: ['ADMIN_SUC'] },
];

export default function Sidebar({ rol }) {
  const location = useLocation();
  const modulosVisibles = MODULOS.filter((m) => m.roles.includes(rol));

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <div className="sidebar-logo-mark">
          <svg width="26" height="26" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="scGlowLinearSidebar" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stopColor="#06B6D4" />
                <stop offset="50%" stopColor="#6366F1" />
                <stop offset="100%" stopColor="#3B82F6" />
              </linearGradient>
            </defs>
            <path
              d="M14 16C14 13.2386 16.2386 11 19 11H30C32.7614 11 35 13.2386 35 16V19C35 21.7614 32 24 30 24H18C15.2386 24 13 26.2386 13 29V32C13 34.7614 15.2386 37 18 37H29C31.7614 37 34 34.7614 34 32"
              stroke="url(#scGlowLinearSidebar)"
              strokeWidth="4"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            <circle cx="14" cy="16" r="3" fill="#06B6D4" />
            <circle cx="34" cy="32" r="3" fill="#3B82F6" />
          </svg>
        </div>
        <span className="sidebar-logo-text">
          SUCargo
          <span className="sidebar-logo-sub">OMS · WMS</span>
        </span>
      </div>

      <nav className="sidebar-nav">
        {modulosVisibles.map((m) => (
          <Link
            key={m.nombre}
            to={m.ruta}
            className={`sidebar-link ${location.pathname === m.ruta ? 'sidebar-link-activo' : ''}`}
          >
            {m.nombre}
          </Link>
        ))}
      </nav>
    </aside>
  );
}