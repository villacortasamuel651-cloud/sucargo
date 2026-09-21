import { useState, useEffect } from 'react';
import './ClienteModal.css';

const VACIO = { razonSocial: '', rucDni: '', telefono: '' };

export default function ClienteModal({ cliente, onGuardar, onCerrar }) {
  const [form, setForm] = useState(VACIO);
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  const esEdicion = !!cliente;

  useEffect(() => {
    if (cliente) {
      setForm({
        razonSocial: cliente.razonSocial,
        rucDni: cliente.rucDni,
        telefono: cliente.telefono || '',
      });
    } else {
      setForm(VACIO);
    }
  }, [cliente]);

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (!form.razonSocial.trim() || !form.rucDni.trim()) {
      setError('Razón social y RUC/DNI son obligatorios.');
      return;
    }

    setGuardando(true);
    try {
      await onGuardar(form, cliente?.id);
    } catch (err) {
      if (err.response?.status === 400) {
        setError(err.response.data?.message || 'Datos inválidos o RUC ya registrado.');
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
        <h2>{esEdicion ? 'Editar cliente' : 'Nuevo cliente'}</h2>

        <form onSubmit={handleSubmit} className="modal-form">
          <label>Razón social</label>
          <input
            name="razonSocial"
            value={form.razonSocial}
            onChange={handleChange}
            placeholder="Bodega La Esquina"
          />

          <label>RUC / DNI</label>
          <input
            name="rucDni"
            value={form.rucDni}
            onChange={handleChange}
            placeholder="10458796321"
          />

          <label>Teléfono</label>
          <input
            name="telefono"
            value={form.telefono}
            onChange={handleChange}
            placeholder="987654321"
          />

          {error && <p className="modal-error">{error}</p>}

          <div className="modal-acciones">
            <button type="button" className="modal-cancelar" onClick={onCerrar}>
              Cancelar
            </button>
            <button type="submit" disabled={guardando}>
              {guardando ? 'Guardando…' : 'Guardar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
