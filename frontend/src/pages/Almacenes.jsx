import { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import AlmacenDetail from '../components/AlmacenDetail';
import AlmacenModal from '../components/AlmacenModal';
import { useAuth } from '../context/AuthContext';
import {
  listarAlmacenes,
  obtenerAlmacen,
  crearAlmacen,
  editarAlmacen,
  desactivarAlmacen,
} from '../services/almacenes';
import './Almacenes.css';

export default function Almacenes() {
  const { usuario } = useAuth();

  const [almacenes, setAlmacenes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [seleccionado, setSeleccionado] = useState(null);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [editando, setEditando] = useState(null);

  useEffect(() => {
    cargarAlmacenes();
  }, []);

  async function cargarAlmacenes() {
    setCargando(true);
    setError('');

    try {
      const data = await listarAlmacenes();
      setAlmacenes(data);
    } catch (err) {
      setError('No se pudo conectar con el servidor.');
    } finally {
      setCargando(false);
    }
  }

  async function handleSeleccionar(item) {
    try {
      const detalle = await obtenerAlmacen(item.id);
      setSeleccionado(detalle);
    } catch (err) {
      setError('No se pudo obtener la información del almacén.');
    }
  }

  function handleActualizarDetalle(actualizado) {
    setSeleccionado(actualizado);
    cargarAlmacenes();
  }

  async function handleGuardar(form, id) {
    try {
      if (id) {
        await editarAlmacen(id, form);
      } else {
        await crearAlmacen(form);
      }

      setModalAbierto(false);
      setEditando(null);
      cargarAlmacenes();
    } catch (err) {
      throw err;
    }
  }

  async function handleDesactivar(item) {
    if (!confirm(`¿Desactivar ${item.nombre}?`)) {
      return;
    }

    try {
      await desactivarAlmacen(item.id);

      if (seleccionado?.id === item.id) {
        setSeleccionado(null);
      }

      cargarAlmacenes();
    } catch (err) {
      setError('No se pudo desactivar el almacén.');
    }
  }

  return (
    <div className="dashboard-layout">
      <Sidebar rol={usuario.rol} />

      <div className="dashboard-content">

        <header className="almacenes-header">
          <h2>Almacenes</h2>

          <button
            className="almacenes-nuevo-btn"
            onClick={() => {
              setEditando(null);
              setModalAbierto(true);
            }}
          >
            + Nuevo almacén
          </button>
        </header>

        <main className="almacenes-main">

          {cargando && (
            <p>Cargando almacenes…</p>
          )}

          {error && (
            <p className="almacenes-error">
              {error}
            </p>
          )}

          {!cargando && !error && (
            <div className="almacenes-layout">

              <div className="almacenes-lista">

                {almacenes.length === 0 && (
                  <p className="almacenes-vacio">
                    Todavía no hay almacenes registrados.
                  </p>
                )}

                {almacenes.map((a) => (
                  <div
                    key={a.id}
                    className="almacenes-item-wrapper"
                  >

                    <button
                      className={`almacenes-item ${
                        seleccionado?.id === a.id
                          ? 'almacenes-item-activo'
                          : ''
                      }`}
                      onClick={() => handleSeleccionar(a)}
                    >
                      <span className="almacenes-item-nombre">
                        {a.nombre}
                      </span>

                      <span className="almacenes-item-sub">
                        {a.direccion || 'Sin dirección registrada'}
                      </span>
                    </button>

                    <div className="almacenes-item-acciones">

                      <button
                        onClick={() => {
                          setEditando(a);
                          setModalAbierto(true);
                        }}
                      >
                        Editar
                      </button>

                      <button
                        className="almacenes-desactivar"
                        onClick={() => handleDesactivar(a)}
                      >
                        Desactivar
                      </button>

                    </div>

                  </div>
                ))}

              </div>

              <div className="almacenes-detail-panel">

                {seleccionado ? (
                  <AlmacenDetail
                    almacen={seleccionado}
                    onActualizar={handleActualizarDetalle}
                    onCerrar={() => setSeleccionado(null)}
                  />
                ) : (
                  <p className="almacenes-vacio">
                    Selecciona un almacén para ver sus datos.
                  </p>
                )}

              </div>

            </div>
          )}

        </main>
      </div>

      {modalAbierto && (
        <AlmacenModal
          almacen={editando}
          onGuardar={handleGuardar}
          onCerrar={() => {
            setModalAbierto(false);
            setEditando(null);
          }}
        />
      )}

    </div>
  );
}