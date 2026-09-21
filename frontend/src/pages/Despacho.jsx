import { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import DespachoDetailModal from '../components/DespachoDetailModal';
import { useAuth } from '../context/AuthContext';
import { listarPedidos } from '../services/pedidos';
import { listarDespachos, obtenerDespacho, iniciarDespacho } from '../services/despacho';
import './Despacho.css';
import '../components/DespachoDetailModal.css';

const ROLES_EJECUTORES = ['LOGISTICA', 'DESPACHO'];

export default function Despacho() {
  const { usuario } = useAuth();
  const puedeGestionar = ROLES_EJECUTORES.includes(usuario.rol);

  const [despachos, setDespachos] = useState([]);
  const [pedidosListos, setPedidosListos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [detalle, setDetalle] = useState(null);

  useEffect(() => {
    cargarTodo();
  }, []);

  async function cargarTodo() {
    setCargando(true);
    setError('');
    try {
      const [desp, pedidos] = await Promise.all([
        listarDespachos(),
        listarPedidos({ estado: 'LISTO_PARA_DESPACHO' }),
      ]);
      setDespachos(desp);
      setPedidosListos(pedidos);
    } catch (err) {
      setError('No se pudo conectar con el servidor.');
    } finally {
      setCargando(false);
    }
  }

  async function handleIniciar(pedidoId) {
    try {
      await iniciarDespacho(pedidoId);
      cargarTodo();
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo iniciar el despacho.');
    }
  }

  async function abrirDetalle(d) {
    const completo = await obtenerDespacho(d.id);
    setDetalle(completo);
  }

  function handleActualizarDetalle(actualizado) {
    setDetalle(actualizado);
    cargarTodo();
  }

  return (
    <div className="dashboard-layout">
      <Sidebar rol={usuario.rol} />

      <div className="dashboard-content">
        <header className="despacho-header">
          <h2>Despacho</h2>
        </header>

        <main className="despacho-main">
          {puedeGestionar && pedidosListos.length > 0 && (
            <section className="despacho-section">
              <h3>Pedidos listos para despachar</h3>
              <div className="despacho-listos-grid">
                {pedidosListos.map((p) => (
                  <div key={p.id} className="despacho-listo-card">
                    <strong>{p.codigo}</strong>
                    <span>{p.cliente.razonSocial}</span>
                    <button onClick={() => handleIniciar(p.id)}>Iniciar despacho</button>
                  </div>
                ))}
              </div>
            </section>
          )}

          <section className="despacho-section">
            <h3>Despachos</h3>
            {cargando && <p>Cargando…</p>}
            {error && <p className="despacho-error">{error}</p>}
            {!cargando && !error && despachos.length === 0 && (
              <p className="despacho-vacio">Todavía no hay despachos iniciados.</p>
            )}

            {!cargando && !error && despachos.length > 0 && (
              <table className="despacho-tabla">
                <thead>
                  <tr><th>Pedido</th><th>Bultos verificados</th><th>Estado</th></tr>
                </thead>
                <tbody>
                  {despachos.map((d) => (
                    <tr key={d.id} className="despacho-fila" onClick={() => abrirDetalle(d)}>
                      <td>{d.pedido.codigo}</td>
                      <td>{d.bultosVerificados}/{d.totalBultos}</td>
                      <td><span className={`inv-estado-badge despacho-estado-${d.estado}`}>{d.estado}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </section>
        </main>
      </div>

      {detalle && (
        <DespachoDetailModal despacho={detalle} onActualizar={handleActualizarDetalle} onCerrar={() => setDetalle(null)} />
      )}
    </div>
  );
}