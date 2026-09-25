import { useState } from 'react';
import { crearCategoria } from '../services/productos';
import './Productomodal.css';
import './CategoriaManagerModal.css';

export default function CategoriaManagerModal({ categorias, onCategoriaCreada, onCerrar }) {
  const [nombre, setNombre] = useState('');
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  async function handleAgregar(e) {
    e.preventDefault();
    if (!nombre.trim()) return;

    setError('');
    setGuardando(true);
    try {
      const nueva = await crearCategoria(nombre.trim());
      onCategoriaCreada(nueva);
      setNombre('');
    } catch (err) {
      setError('No se pudo crear la categoría.');
    } finally {
      setGuardando(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <h2>Categorías de producto</h2>

        {categorias.length === 0 ? (
          <p className="categoria-vacio">Todavía no hay categorías. Cada empresa define las suyas.</p>
        ) : (
          <ul className="categoria-lista">
            {categorias.map((c) => (
              <li key={c.id} className="categoria-item">{c.nombre}</li>
            ))}
          </ul>
        )}

        <form onSubmit={handleAgregar} className="modal-form">
          <label>Nueva categoría</label>
          <input value={nombre} onChange={(e) => setNombre(e.target.value)} placeholder="Ej. Limpieza" />
          {error && <p className="modal-error">{error}</p>}

          <div className="modal-acciones">
            <button type="button" className="modal-cancelar" onClick={onCerrar}>Cerrar</button>
            <button type="submit" disabled={guardando}>{guardando ? 'Agregando…' : '+ Agregar'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}