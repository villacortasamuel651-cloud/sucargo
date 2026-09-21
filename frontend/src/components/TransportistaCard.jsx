import './TransportistaCard.css';

export default function TransportistaCard({ transportista, onEditar, onDesactivar, onActivar }) {
  const inactivo = transportista.estado === 'INACTIVO';

  return (
    <div className={`transportista-card ${inactivo ? 'transportista-card-inactivo' : ''}`}>
      <div className="transportista-card-header">
        <h3>{transportista.razonSocial}</h3>
        <span className={`transportista-badge ${inactivo ? 'transportista-badge-inactivo' : ''}`}>
          {transportista.estado}
        </span>
      </div>

      <p className="transportista-card-dato">
        <span>RUC</span> {transportista.ruc || '—'}
      </p>
      <p className="transportista-card-dato">
        <span>Teléfono</span> {transportista.telefono || '—'}
      </p>
      <p className="transportista-card-dato">
        <span>Tipo de servicio</span> {transportista.tipoServicio || '—'}
      </p>

      <div className="transportista-card-acciones">
        <button onClick={() => onEditar(transportista)}>Editar</button>
        {inactivo ? (
          <button className="transportista-card-activar" onClick={() => onActivar(transportista)}>
            Activar
          </button>
        ) : (
          <button className="transportista-card-desactivar" onClick={() => onDesactivar(transportista)}>
            Desactivar
          </button>
        )}
      </div>
    </div>
  );
}