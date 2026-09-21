import { useState, useEffect, useMemo } from 'react';
import Sidebar from '../components/Sidebar';
import ProductoCard from '../components/ProductoCard';
import ProductoModal from '../components/ProductoModal';
import ProductoDetailModal from '../components/ProductoDetailModal';
import CategoriaManagerModal from '../components/CategoriaManagerModal';
import { useAuth } from '../context/AuthContext';
import {
  listarProductos,
  listarCategorias,
  crearProducto,
  editarProducto,
  desactivarProducto,
} from '../services/productos';
import './Productos.css';

export default function Productos() {
  const { usuario } = useAuth();
  const [productos, setProductos] = useState([]);
  const [categorias, setCategorias] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [modalAbierto, setModalAbierto] = useState(false);
  const [productoEditando, setProductoEditando] = useState(null);
  const [categoriasModalAbierto, setCategoriasModalAbierto] = useState(false);
  const [productoDetalle, setProductoDetalle] = useState(null);

  const [busqueda, setBusqueda] = useState('');
  const [categoriaFiltro, setCategoriaFiltro] = useState('TODAS');

  useEffect(() => {
    cargarDatos();
  }, []);

  async function cargarDatos() {
    setCargando(true);
    setError('');
    try {
      const [prods, cats] = await Promise.all([listarProductos(), listarCategorias()]);
      setProductos(prods);
      setCategorias(cats);
    } catch (err) {
      setError('No se pudo conectar con el servidor.');
    } finally {
      setCargando(false);
    }
  }

  function abrirNuevo() {
    setProductoEditando(null);
    setModalAbierto(true);
  }

  function abrirEditar(producto) {
    setProductoEditando(producto);
    setModalAbierto(true);
  }

  async function handleGuardar(form, id) {
    if (id) {
      await editarProducto(id, form);
    } else {
      await crearProducto(form);
    }
    setModalAbierto(false);
    cargarDatos();
  }

  async function handleDesactivar(producto) {
    if (!confirm(`¿Desactivar ${producto.nombre}?`)) return;
    await desactivarProducto(producto.id);
    cargarDatos();
  }

  // ---- Stats de resumen, derivadas de los mismos datos ----
  const stats = useMemo(() => {
    const total = productos.length;
    const activos = productos.filter((p) => p.estado !== 'INACTIVO').length;
    // "Stock bajo" real depende del módulo de Inventario (stock actual vs mínimo);
    // aquí no tenemos stock actual, así que este valor queda en 0 hasta conectar
    // ese dato — no lo inventamos.
    const stockBajo = 0;
    return { total, activos, stockBajo };
  }, [productos]);

  // ---- Filtro búsqueda + categoría, client-side ----
  const productosFiltrados = useMemo(() => {
    return productos.filter((p) => {
      const coincideTexto =
        p.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
        p.sku.toLowerCase().includes(busqueda.toLowerCase());
      const coincideCategoria =
        categoriaFiltro === 'TODAS' || p.categoria?.id === categoriaFiltro;
      return coincideTexto && coincideCategoria;
    });
  }, [productos, busqueda, categoriaFiltro]);

  return (
    <div className="dashboard-layout">
      <Sidebar rol={usuario.rol} />

      <div className="dashboard-content">
        <header className="productos-header">
          <h2>Productos</h2>
          <div className="productos-header-acciones">
            <button className="btn-secondary" onClick={() => setCategoriasModalAbierto(true)}>
              Categorías
            </button>
            <button className="btn-primary" onClick={abrirNuevo}>+ Nuevo producto</button>
          </div>
        </header>

        <main className="productos-main">
          {cargando && <p>Cargando productos…</p>}
          {error && <p className="productos-error">{error}</p>}

          {!cargando && !error && productos.length === 0 && (
            <p className="productos-vacio">Todavía no hay productos registrados.</p>
          )}

          {!cargando && !error && productos.length > 0 && (
            <>
              {/* ---- Stats de resumen ---- */}
              <div className="productos-stats">
                <div className="stat-card" style={{ '--stat-color': '#6366F1' }}>
                  <span className="stat-label">Total</span>
                  <span className="stat-value">{stats.total}</span>
                </div>
                <div className="stat-card" style={{ '--stat-color': '#15803D' }}>
                  <span className="stat-label">Activos</span>
                  <span className="stat-value">{stats.activos}</span>
                </div>
                <div className="stat-card" style={{ '--stat-color': '#B91C1C' }}>
                  <span className="stat-label">Stock bajo</span>
                  <span className="stat-value">{stats.stockBajo}</span>
                </div>
              </div>

              {/* ---- Buscador + filtro por categoría ---- */}
              <div className="productos-toolbar">
                <input
                  className="productos-buscador"
                  type="text"
                  placeholder="Buscar por nombre o SKU…"
                  value={busqueda}
                  onChange={(e) => setBusqueda(e.target.value)}
                />
                <div className="productos-cat-filtros">
                  <button
                    className={`cat-chip ${categoriaFiltro === 'TODAS' ? 'cat-chip-activo' : ''}`}
                    onClick={() => setCategoriaFiltro('TODAS')}
                  >
                    Todas
                  </button>
                  {categorias.map((c) => (
                    <button
                      key={c.id}
                      className={`cat-chip ${categoriaFiltro === c.id ? 'cat-chip-activo' : ''}`}
                      onClick={() => setCategoriaFiltro(c.id)}
                    >
                      {c.nombre}
                    </button>
                  ))}
                </div>
              </div>

              {productosFiltrados.length === 0 && (
                <p className="productos-vacio">No se encontraron productos con ese criterio.</p>
              )}

              <div className="productos-grid">
                {productosFiltrados.map((p) => (
                  <ProductoCard
                    key={p.id}
                    producto={p}
                    onEditar={abrirEditar}
                    onDesactivar={handleDesactivar}
                    onVerDetalle={setProductoDetalle}
                  />
                ))}

                {/* Card fantasma: atajo visual para crear, además del botón del header */}
                <button className="producto-card-agregar" onClick={abrirNuevo}>
                  <span className="producto-card-agregar-icono">+</span>
                  <span>Agregar producto</span>
                </button>
              </div>
            </>
          )}
        </main>
      </div>

      {modalAbierto && (
        <ProductoModal
          producto={productoEditando}
          categorias={categorias}
          onGuardar={handleGuardar}
          onCerrar={() => setModalAbierto(false)}
        />
      )}

      {productoDetalle && (
        <ProductoDetailModal
          producto={productoDetalle}
          onEditar={abrirEditar}
          onDesactivar={handleDesactivar}
          onCerrar={() => setProductoDetalle(null)}
        />
      )}

      {categoriasModalAbierto && (
        <CategoriaManagerModal
          categorias={categorias}
          onCategoriaCreada={(nueva) => setCategorias((prev) => [...prev, nueva])}
          onCerrar={() => setCategoriasModalAbierto(false)}
        />
      )}
    </div>
  );
}