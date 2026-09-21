import DetailTabs from './DetailTabs';
import './AlmacenDetail.css';

export default function AlmacenDetail({ almacen, onCerrar }) {
  const tabs = [
    {
      id: 'info',
      label: 'Datos generales',
      content: (
        <div className="almacen-tab-section">

          <label>Nombre</label>
          <p className="almacen-dato-readonly">
            {almacen.nombre}
          </p>

          <label>Dirección</label>
          <p className="almacen-dato-readonly">
            {almacen.direccion || '—'}
          </p>

          <label>Estado</label>
          <span
            className={`almacen-badge ${
              almacen.estado === 'INACTIVO'
                ? 'almacen-badge-inactivo'
                : ''
            }`}
          >
            {almacen.estado}
          </span>

        </div>
      ),
    },
  ];

  return (
    <div className="almacen-detail">

      <div className="almacen-detail-header">
        <h2>{almacen.nombre}</h2>

        <button
          className="almacen-detail-cerrar"
          onClick={onCerrar}
        >
          Cerrar
        </button>
      </div>

      <DetailTabs tabs={tabs} />

    </div>
  );
}