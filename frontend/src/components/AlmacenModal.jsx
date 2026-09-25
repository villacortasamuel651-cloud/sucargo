import { useState, useEffect } from 'react';
import './Almacenmodal.css';

const VACIO = { nombre: '', direccion: '' };

export default function AlmacenModal({ almacen, onGuardar, onCerrar }) {
  const [form, setForm] = useState(VACIO);
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    setForm(almacen ? { nombre: almacen.nombre, direccion: almacen.direccion || '' } : VACIO);
  }, [almacen]);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (!form.nombre.trim()) {
      setError('El nombre es obligatorio.');
      return;
    }

    setGuardando(true);
    try {
      await onGuardar(form, almacen?.id);
    } catch (err) {
      setError('No se pudo guardar. Intenta de nuevo.');
    } finally {
      setGuardando(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <h2>{almacen ? 'Editar almacén' : 'Nuevo almacén'}</h2>
        <form onSubmit={handleSubmit} className="modal-form">
          <label>Nombre</label>
          <input
            value={form.nombre}
            onChange={(e) => setForm({ ...form, nombre: e.target.value })}
            placeholder="Almacén Central"
          />

          <label>Dirección</label>
          <input
            value={form.direccion}
            onChange={(e) => setForm({ ...form, direccion: e.target.value })}
            placeholder="Av. Los Héroes 1580"
          />

          {error && <p className="modal-error">{error}</p>}

          <div className="modal-acciones">
            <button type="button" className="modal-cancelar" onClick={onCerrar}>Cancelar</button>
            <button type="submit" disabled={guardando}>{guardando ? 'Guardando…' : 'Guardar'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}