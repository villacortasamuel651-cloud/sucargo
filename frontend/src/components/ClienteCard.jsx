import './ClienteCard.css';

export default function ClienteCard({ cliente, onEditar, onDesactivar, onVerDetalle }) {
  const inactivo = cliente.estado === 'INACTIVO';

  return (
    <div className={`cliente-card ${inactivo ? 'cliente-card-inactivo' : ''}`}>
      <div className="cliente-card-header">
        <h3>{cliente.razonSocial}</h3>
        <span className={`cliente-badge ${inactivo ? 'cliente-badge-inactivo' : ''}`}>
          {cliente.estado}
        </span>
      </div>

      <p className="cliente-card-dato">
        <span>RUC/DNI</span> {cliente.rucDni}
      </p>
      <p className="cliente-card-dato">
        <span>Teléfono</span> {cliente.telefono || '—'}
      </p>
      <p className="cliente-card-dato">
        <span>Puntos de entrega</span> {cliente.cantidadPuntosEntrega ?? 0}
      </p>

      <div className="cliente-card-acciones">
        <button onClick={() => onVerDetalle(cliente)}>Puntos de entrega</button>
        <button onClick={() => onEditar(cliente)}>Editar</button>
        {!inactivo && (
          <button className="cliente-card-desactivar" onClick={() => onDesactivar(cliente)}>
            Desactivar
          </button>
        )}
      </div>
    </div>
  );
}