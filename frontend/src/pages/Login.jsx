import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '../services/api';
import { useAuth } from '../context/AuthContext';
import './Login.css';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Autenticación y navegación
  const { iniciarSesion } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();

    // Limpiar error anterior
    setError('');

    // Validación básica
    if (!email || !password) {
      setError('Por favor completa todos los campos.');
      return;
    }

    setLoading(true);

    try {
      // 🔐 Login real contra el backend
      const data = await login(email, password);

      // 💾 Guardar sesión / token / datos del usuario
      iniciarSesion(data);

      // 🚀 Redirigir al dashboard
      navigate('/dashboard');

    } catch (err) {
      console.error('Error al iniciar sesión:', err);

      if (err.response?.status === 401) {
        setError('Correo o contraseña incorrectos.');
      } else if (err.response?.status === 403) {
        setError('Tu usuario está inactivo. Contacta al administrador.');
      } else {
        setError('No se pudo conectar con el servidor. Intenta de nuevo.');
      }

    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">

      {/* =====================================================
          FONDO NEÓN DINÁMICO
      ====================================================== */}
      <div className="bg-glow bg-glow-1"></div>
      <div className="bg-glow bg-glow-2"></div>
      <div className="bg-grid"></div>

      <div className="login-card">

        {/* =====================================================
            PANEL IZQUIERDO
        ====================================================== */}
        <div className="login-side">

          {/* LOGO SUCARGO */}
          <div className="login-brand">

            <div className="login-brand-logo">
              <svg
                width="44"
                height="44"
                viewBox="0 0 48 48"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >

                <defs>
                  <linearGradient
                    id="scGlowLinear"
                    x1="0%"
                    y1="0%"
                    x2="100%"
                    y2="100%"
                  >
                    <stop offset="0%" stopColor="#06B6D4" />
                    <stop offset="50%" stopColor="#6366F1" />
                    <stop offset="100%" stopColor="#3B82F6" />
                  </linearGradient>
                </defs>

                {/* Isotipo S + Flujo de Carga */}
                <path
                  className="logo-path"
                  d="M14 16C14 13.2386 16.2386 11 19 11H30C32.7614 11 35 13.2386 35 16V19C35 21.7614 32 24 30 24H18C15.2386 24 13 26.2386 13 29V32C13 34.7614 15.2386 37 18 37H29C31.7614 37 34 34.7614 34 32"
                  stroke="url(#scGlowLinear)"
                  strokeWidth="4"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />

                <circle
                  cx="14"
                  cy="16"
                  r="3"
                  fill="#06B6D4"
                  className="node-pulse"
                />

                <circle
                  cx="34"
                  cy="32"
                  r="3"
                  fill="#3B82F6"
                  className="node-pulse-delay"
                />

              </svg>
            </div>

            <div className="login-brand-text">
              <span className="login-brand-title">
                SUCargo
              </span>

              <span className="login-brand-tag">
                OMS · WMS
              </span>
            </div>

          </div>

          {/* =====================================================
              CONTENIDO PRINCIPAL
          ====================================================== */}
          <div className="login-side-content">

            <h1 className="login-tagline">
              Del pedido al despacho,
              <br />
              todo en un solo lugar.
            </h1>

            <p className="login-copy">
              Gestiona pedidos, inventario, almacén y planificación
              de despachos de tu distribuidora desde una sola plataforma.
            </p>

            {/* =================================================
                ILUSTRACIÓN
            ================================================== */}
            <div className="login-illustration-container">

              <svg
                className="login-illustration"
                viewBox="0 0 300 120"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >

                {/* Red de nodos WMS */}
                <g className="grid-nodes">

                  <line
                    x1="20"
                    y1="95"
                    x2="280"
                    y2="95"
                    stroke="rgba(255, 255, 255, 0.15)"
                    strokeWidth="2"
                    strokeDasharray="6 6"
                  />

                  <path
                    d="M20 95L55 45L90 95"
                    stroke="rgba(99, 102, 241, 0.3)"
                    strokeWidth="1.5"
                    strokeDasharray="4 4"
                  />

                  <circle
                    cx="55"
                    cy="45"
                    r="4"
                    fill="#6366F1"
                  />

                </g>

                {/* Almacén Inteligente */}
                <rect
                  x="15"
                  y="55"
                  width="45"
                  height="40"
                  rx="6"
                  stroke="rgba(255, 255, 255, 0.3)"
                  strokeWidth="2"
                  fill="rgba(15, 23, 42, 0.4)"
                />

                <path
                  d="M27 70H48M27 78H42"
                  stroke="#06B6D4"
                  strokeWidth="2"
                  strokeLinecap="round"
                />

                {/* Camión Futurista OMS */}
                <g className="truck-group">

                  <rect
                    x="105"
                    y="50"
                    width="75"
                    height="35"
                    rx="6"
                    stroke="#818CF8"
                    strokeWidth="2"
                    fill="#1E1B4B"
                  />

                  <path
                    d="M180 62H202L212 74V85H180V62Z"
                    stroke="#818CF8"
                    strokeWidth="2"
                    fill="#312E81"
                  />

                  <rect
                    x="190"
                    y="66"
                    width="12"
                    height="8"
                    rx="2"
                    fill="#06B6D4"
                  />

                  {/* Neumático */}
                  <g className="wheel">

                    <circle
                      cx="128"
                      cy="88"
                      r="7"
                      stroke="#6366F1"
                      strokeWidth="2"
                      fill="#0F172A"
                    />

                    <circle
                      cx="128"
                      cy="88"
                      r="2.5"
                      fill="#06B6D4"
                    />

                  </g>

                  {/* Neumático */}
                  <g className="wheel">

                    <circle
                      cx="195"
                      cy="88"
                      r="7"
                      stroke="#6366F1"
                      strokeWidth="2"
                      fill="#0F172A"
                    />

                    <circle
                      cx="195"
                      cy="88"
                      r="2.5"
                      fill="#06B6D4"
                    />

                  </g>

                </g>

                {/* Contenedores */}
                <g className="packages">

                  <rect
                    x="240"
                    y="65"
                    width="22"
                    height="22"
                    rx="4"
                    stroke="#06B6D4"
                    strokeWidth="2"
                    fill="rgba(6, 182, 212, 0.15)"
                  />

                  <rect
                    x="246"
                    y="40"
                    width="22"
                    height="22"
                    rx="4"
                    stroke="#818CF8"
                    strokeWidth="2"
                    fill="rgba(129, 140, 248, 0.15)"
                  />

                </g>

              </svg>

            </div>

          </div>

          {/* FOOTER */}
          <div className="login-side-footer">

            <span className="badge-tech">
              SUC-OMS-WMS
            </span>

            <span className="badge-version">
              v1.0
            </span>

          </div>

        </div>

        {/* =====================================================
            PANEL DERECHO - LOGIN
        ====================================================== */}
        <div className="login-form-panel">

          <div className="form-header">

            <h2>
              Iniciar sesión
            </h2>

            <p className="login-form-sub">
              Ingresa con tu cuenta de empresa registrada.
            </p>

          </div>

          {/* =================================================
              FORMULARIO
          ================================================== */}
          <form
            className="login-form"
            onSubmit={handleSubmit}
          >

            {/* CORREO */}
            <div className="input-group">

              <label htmlFor="email">
                CORREO ELECTRÓNICO
              </label>

              <div className="input-wrapper">

                <svg
                  className="input-icon"
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                >

                  <path
                    d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"
                  />

                  <polyline
                    points="22,6 12,13 2,6"
                  />

                </svg>

                <input
                  id="email"
                  type="email"
                  placeholder="nombre@empresa.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  autoComplete="username"
                  required
                />

              </div>

            </div>

            {/* CONTRASEÑA */}
            <div className="input-group">

              <label htmlFor="password">
                CONTRASEÑA
              </label>

              <div className="input-wrapper">

                <svg
                  className="input-icon"
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                >

                  <rect
                    x="3"
                    y="11"
                    width="18"
                    height="11"
                    rx="2"
                    ry="2"
                  />

                  <path
                    d="M7 11V7a5 5 0 0 1 10 0v4"
                  />

                </svg>

                <input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="current-password"
                  required
                />

              </div>

            </div>

            {/* ACCIONES */}
            <div className="form-actions">

              <a
                href="/olvide-password"
                className="login-forgot"
              >
                ¿Olvidaste tu contraseña?
              </a>

            </div>

            {/* ERROR */}
            {error && (
              <div className="login-error">
                {error}
              </div>
            )}

            {/* BOTÓN */}
            <button
              type="submit"
              className="login-btn"
              disabled={loading}
            >

              {loading ? (

                <span className="spinner"></span>

              ) : (

                <>
                  <span>
                    Ingresar
                  </span>

                  <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2.5"
                  >

                    <line
                      x1="5"
                      y1="12"
                      x2="19"
                      y2="12"
                    />

                    <polyline
                      points="12 5 19 12 12 19"
                    />

                  </svg>

                </>

              )}

            </button>

          </form>

          {/* FOOTNOTE */}
          <p className="login-footnote">

            ¿Problemas para ingresar?{' '}

            <a href="#support">
              Contacta a tu administrador
            </a>

          </p>

        </div>

      </div>

    </div>
  );
}

