import { useState, useEffect } from 'react';
import './Productomodal.css';

const VACIO = {
  sku: '', nombre: '', descripcion: '', categoriaId: '',
  unidadMedida: '', peso: '', stockMinimo: 0,
};

export default function ProductoModal({ producto, categorias, onGuardar, onCerrar }) {
  const [form, setForm] = useState(VACIO);
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  const esEdicion = !!producto;

  useEffect(() => {
    if (producto) {
      setForm({
        sku: producto.sku,
        nombre: producto.nombre,
        descripcion: producto.descripcion || '',
        categoriaId: producto.categoria?.id || '',
        unidadMedida: producto.unidadMedida,
        peso: producto.peso ?? '',
        stockMinimo: producto.stockMinimo,
      });
    } else {
      setForm(VACIO);
    }
  }, [producto]);

  function handleChange(e) {
    const { name, value } = e.target;
    const esNumerico = name === 'stockMinimo' || name === 'peso';
    setForm({ ...form, [name]: esNumerico ? (value === '' ? '' : Number(value)) : value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (!form.sku.trim() || !form.nombre.trim() || !form.unidadMedida.trim()) {
      setError('SKU, nombre y unidad de medida son obligatorios.');
      return;
    }

    setGuardando(true);
    try {
      await onGuardar(form, producto?.id);
    } catch (err) {
      if (err.response?.status === 400) {
        setError(err.response.data?.message || 'Datos inválidos o SKU ya registrado.');
      } else {
        setError('No se pudo guardar. Intenta de nuevo.');
      }
    } finally {
      setGuardando(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <h2>{esEdicion ? 'Editar producto' : 'Nuevo producto'}</h2>

        <form onSubmit={handleSubmit} className="modal-form">
          <label>SKU</label>
          <input name="sku" value={form.sku} onChange={handleChange} placeholder="DET-001" />

          <label>Nombre</label>
          <input name="nombre" value={form.nombre} onChange={handleChange} placeholder="Detergente Ariel 1kg" />

          <label>Descripción</label>
          <input name="descripcion" value={form.descripcion} onChange={handleChange} placeholder="Opcional" />

          <label>Categoría</label>
          <select name="categoriaId" value={form.categoriaId} onChange={handleChange}>
            <option value="">Sin categoría</option>
            {categorias.map((c) => (
              <option key={c.id} value={c.id}>{c.nombre}</option>
            ))}
          </select>

          <label>Unidad de medida</label>
          <input name="unidadMedida" value={form.unidadMedida} onChange={handleChange} placeholder="UND, KG, CAJA..." />

          <label>Peso (kg)</label>
          <input type="number" step="0.01" name="peso" value={form.peso} onChange={handleChange} placeholder="Opcional" min="0" />

          <label>Stock mínimo</label>
          <input type="number" name="stockMinimo" value={form.stockMinimo} onChange={handleChange} min="0" />

          {/* Placeholder visual: aún no conectado a subida real de archivos.
              Cuando armemos el endpoint de imágenes, aquí va un <input type="file">
              que suba y guarde la URL en el form. */}
          <div className="imagen-upload-placeholder" title="Disponible cuando conectemos el backend de imágenes">
            <span className="imagen-upload-icono">📷</span>
            <span>Subir imagen (próximamente)</span>
          </div>

          {error && <p className="modal-error">{error}</p>}

          <div className="modal-acciones">
            <button type="button" className="modal-cancelar" onClick={onCerrar}>Cancelar</button>
            <button type="submit" disabled={guardando}>
              {guardando ? 'Guardando…' : 'Guardar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}