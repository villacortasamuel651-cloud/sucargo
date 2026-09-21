import './ProductoCard.css';

export default function ProductoCard({ producto, onEditar, onDesactivar, onVerDetalle }) {
  const inactivo = producto.estado === 'INACTIVO';

  return (
    <div
      className={`producto-card ${inactivo ? 'producto-card-inactivo' : ''}`}
      onClick={() => onVerDetalle(producto)}
    >
      <div className="producto-imagen">
        {producto.imagenUrl ? (
          <img src={producto.imagenUrl} alt={producto.nombre} />
        ) : (
          // Placeholder mientras no exista subida de imágenes conectada al backend.
          <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="#6366F1" strokeWidth="1.5">
            <path d="M21 8L12 3 3 8v8l9 5 9-5V8z" />
            <path d="M3 8l9 5 9-5M12 13v8" />
          </svg>
        )}
      </div>

      <div className="producto-card-body">
        <div className="producto-card-header">
          <div>
            <h3>{producto.nombre}</h3>
            <span className="producto-sku">{producto.sku}</span>
          </div>
          <span className={`producto-badge ${inactivo ? 'producto-badge-inactivo' : ''}`}>
            {producto.estado}
          </span>
        </div>

        {producto.categoria?.nombre && (
          <span className="producto-cat-tag">{producto.categoria.nombre}</span>
        )}

        <p className="producto-dato">
          <span>Unidad</span> {producto.unidadMedida}
        </p>
        <p className="producto-dato">
          <span>Stock mínimo</span> {producto.stockMinimo}
        </p>

        <div className="producto-card-acciones">
          {/* stopPropagation: evita que el clic en estos botones también dispare
              onVerDetalle (que está en el contenedor padre) */}
          <button onClick={(e) => { e.stopPropagation(); onEditar(producto); }}>
            Editar
          </button>
          {!inactivo && (
            <button
              className="producto-card-desactivar"
              onClick={(e) => { e.stopPropagation(); onDesactivar(producto); }}
            >
              Desactivar
            </button>
          )}
        </div>
      </div>
    </div>
  );
}