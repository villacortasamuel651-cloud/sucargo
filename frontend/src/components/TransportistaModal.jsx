import { useState, useEffect } from 'react';
import './ClienteModal.css';

const VACIO = { razonSocial: '', ruc: '', contacto: '', telefono: '', correo: '', tipoServicio: '' };

export default function TransportistaModal({ transportista, onGuardar, onCerrar }) {
  const [form, setForm] = useState(VACIO);
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    setForm(transportista ? { ...VACIO, ...transportista } : VACIO);
  }, [transportista]);

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (!form.razonSocial.trim()) {
      setError('La razón social es obligatoria.');
      return;
    }

    setGuardando(true);
    try {
      await onGuardar(form, transportista?.id);
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo guardar.');
    } finally {
      setGuardando(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <h2>{transportista ? 'Editar transportista' : 'Nuevo transportista'}</h2>

        <form onSubmit={handleSubmit} className="modal-form">
          <label>Razón social</label>
          <input name="razonSocial" value={form.razonSocial} onChange={handleChange} placeholder="Transportes López" />

          <label>RUC</label>
          <input name="ruc" value={form.ruc} onChange={handleChange} placeholder="20458796321" />

          <label>Contacto</label>
          <input name="contacto" value={form.contacto} onChange={handleChange} placeholder="Nombre del contacto" />

          <label>Teléfono</label>
          <input name="telefono" value={form.telefono} onChange={handleChange} placeholder="987654321" />

          <label>Correo</label>
          <input name="correo" value={form.correo} onChange={handleChange} placeholder="contacto@transportes.pe" />

          <label>Tipo de servicio</label>
          <input name="tipoServicio" value={form.tipoServicio} onChange={handleChange} placeholder="Propio, Tercero, Moto…" />

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