import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Clientes from './pages/Clientes';
import Usuarios from './pages/Usuarios';
import Productos from './pages/Productos';
import Almacenes from './pages/Almacenes';
import Inventario from './pages/Inventario';
import Pedidos from './pages/Pedidos';
import Picking from './pages/Picking';
import Packing from './pages/Packing';
import Transportistas from './pages/Transportistas';
import Distribucion from './pages/Distribucion';
import OrdenesTransporte from "./pages/Ordenestransporte";
import Despacho from './pages/Despacho';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/clientes"
            element={
              <ProtectedRoute>
                <Clientes />
              </ProtectedRoute>
            }
          />
          <Route
            path="/usuarios"
            element={
              <ProtectedRoute>
                <Usuarios />
              </ProtectedRoute>
            }
          />
          <Route
            path="/productos"
            element={
              <ProtectedRoute>
                <Productos />
              </ProtectedRoute>
            }
          />
          <Route
            path="/almacenes"
            element={
              <ProtectedRoute>
                <Almacenes />
              </ProtectedRoute>
            }
          />
          <Route
            path="/inventario"
            element={
              <ProtectedRoute>
                <Inventario />
              </ProtectedRoute>
            }
          />
          <Route
            path="/pedidos"
            element={
              <ProtectedRoute>
                <Pedidos />
              </ProtectedRoute>
            }
          />
          <Route
            path="/picking"
            element={
              <ProtectedRoute>
                <Picking />
              </ProtectedRoute>
            }
          />
          <Route
            path="/packing"
            element={
              <ProtectedRoute>
                <Packing />
              </ProtectedRoute>
            }
          />
          <Route
            path="/transportistas"
            element={
              <ProtectedRoute>
                <Transportistas />
              </ProtectedRoute>
            }
          />
          <Route
            path="/distribucion"
            element={
              <ProtectedRoute>
                <Distribucion />
              </ProtectedRoute>
            }
          />
          <Route
            path="/ordenes-transporte"
            element={
              <ProtectedRoute>
                <OrdenesTransporte />
              </ProtectedRoute>
            }
          />
          <Route
            path="/despacho"
            element={
              <ProtectedRoute>
                <Despacho />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;