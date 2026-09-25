import './Productomodal.css';
import './Productodetailmodal.css';

export default function ProductoDetailModal({ producto, onEditar, onDesactivar, onCerrar }) {
  const inactivo = producto.estado === 'INACTIVO';

  function handleEditar() {
    onCerrar();
    onEditar(producto);
  }

  function handleDesactivar() {
    onCerrar();
    onDesactivar(producto);
  }

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div className="producto-detail-card" onClick={(e) => e.stopPropagation()}>

        <div className="producto-detail-imagen">
          {producto.imagenUrl ? (
            <img src={producto.imagenUrl} alt={producto.nombre} />
          ) : (
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#6366F1" strokeWidth="1.3">
              <path d="M21 8L12 3 3 8v8l9 5 9-5V8z" />
              <path d="M3 8l9 5 9-5M12 13v8" />
            </svg>
          )}
          <button className="producto-detail-cerrar" onClick={onCerrar}>Cerrar</button>
        </div>

        <div className="producto-detail-body">
          <div className="producto-detail-top">
            <div>
              <h2>{producto.nombre}</h2>
              <span className="producto-detail-sku">{producto.sku}</span>
            </div>
            <span className={`producto-badge ${inactivo ? 'producto-badge-inactivo' : ''}`}>
              {producto.estado}
            </span>
          </div>

          {producto.categoria?.nombre && (
            <span className="producto-cat-tag">{producto.categoria.nombre}</span>
          )}

          {producto.descripcion && (
            <p className="producto-detail-descripcion">{producto.descripcion}</p>
          )}

          <div className="producto-detail-datos">
            <div className="producto-detail-dato">
              <span className="producto-detail-dato-label">Unidad de medida</span>
              <span className="producto-detail-dato-valor">{producto.unidadMedida}</span>
            </div>
            <div className="producto-detail-dato">
              <span className="producto-detail-dato-label">Peso</span>
              <span className="producto-detail-dato-valor">
                {producto.peso ? `${producto.peso} kg` : '—'}
              </span>
            </div>
            <div className="producto-detail-dato">
              <span className="producto-detail-dato-label">Stock mínimo</span>
              <span className="producto-detail-dato-valor">{producto.stockMinimo}</span>
            </div>
            <div className="producto-detail-dato">
              <span className="producto-detail-dato-label">Categoría</span>
              <span className="producto-detail-dato-valor">{producto.categoria?.nombre || '—'}</span>
            </div>
          </div>

          <div className="producto-detail-acciones">
            <button className="modal-cancelar" onClick={handleEditar}>Editar producto</button>
            {!inactivo && (
              <button className="producto-detail-desactivar" onClick={handleDesactivar}>
                Desactivar
              </button>
            )}
          </div>
        </div>

      </div>
    </div>
  );
}