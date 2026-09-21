import { useState } from 'react';
import './DetailTabs.css';

// Componente genérico: recibe una lista de tabs { id, label, content }
// y maneja cuál está activa. Se usa para Usuarios, y luego para
// Clientes/Productos cuando tengan varias secciones también.
export default function DetailTabs({ tabs, tabInicial }) {
  const [activa, setActiva] = useState(tabInicial || tabs[0]?.id);
  const tabActual = tabs.find((t) => t.id === activa);

  return (
    <div className="detail-tabs">
      <div className="detail-tabs-nav">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            className={`detail-tabs-btn ${activa === tab.id ? 'detail-tabs-btn-activa' : ''}`}
            onClick={() => setActiva(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="detail-tabs-content">{tabActual?.content}</div>
    </div>
  );
}
