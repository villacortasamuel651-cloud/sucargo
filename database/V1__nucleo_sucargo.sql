-- =========================================================
-- SUCargo - Migración V1: Núcleo del modelo de datos
-- Módulos: Empresas/Usuarios, Clientes, Productos, Almacenes, Inventario, Pedidos
-- Convenciones:
--   - PK: UUID (CHAR(36), generado con UUID())
--   - Soft delete: columna `estado` (ACTIVO/INACTIVO) en vez de DELETE físico
--   - Timestamps: created_at / updated_at en todas las tablas
--   - Multi-tenant: empresa_id en tablas raíz (RNF05: nunca confiar en
--     empresa_id enviado por el frontend, siempre debe obtenerse del JWT)
-- =========================================================

-- ---------------------------------------------------------
-- 1. EMPRESA (tenant)
-- ---------------------------------------------------------
CREATE TABLE empresa (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    ruc             VARCHAR(11)   NOT NULL UNIQUE,
    razon_social    VARCHAR(150)  NOT NULL,
    rubro           VARCHAR(100)  NULL,
    direccion       VARCHAR(200)  NULL,
    representante_legal VARCHAR(150) NULL,
    estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------
-- 2. USUARIO
-- empresa_id es NULL solo para ADMIN_SUC (super admin sin empresa)
-- ---------------------------------------------------------
CREATE TABLE usuario (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    empresa_id      CHAR(36)      NULL,
    nombre          VARCHAR(150)  NOT NULL,
    email           VARCHAR(150)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255)  NOT NULL,
    rol             ENUM('ADMIN_SUC','ADMIN_EMPRESA','VENTAS','ALMACENERO',
                         'SUPERVISOR_ALMACEN','LOGISTICA','DESPACHO') NOT NULL,
    estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT chk_usuario_admin_suc_sin_empresa
        CHECK ( (rol = 'ADMIN_SUC' AND empresa_id IS NULL)
             OR (rol <> 'ADMIN_SUC' AND empresa_id IS NOT NULL) )
);

CREATE INDEX idx_usuario_empresa ON usuario(empresa_id);

-- Refresh tokens (RNF03: JWT con refresh token).
-- Se guarda en BD para poder revocar tokens (logout, cambio de contraseña, etc.)
CREATE TABLE refresh_token (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    usuario_id      CHAR(36)      NOT NULL,
    token           VARCHAR(255)  NOT NULL UNIQUE,
    expires_at      DATETIME      NOT NULL,
    revocado        BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_token_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE INDEX idx_refresh_token_usuario ON refresh_token(usuario_id);

-- ---------------------------------------------------------
-- 3. CLIENTE
-- ---------------------------------------------------------
CREATE TABLE cliente (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    empresa_id      CHAR(36)      NOT NULL,
    razon_social    VARCHAR(150)  NOT NULL,
    ruc_dni         VARCHAR(11)   NOT NULL,
    telefono        VARCHAR(20)   NULL,
    estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cliente_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT uq_cliente_empresa_ruc UNIQUE (empresa_id, ruc_dni)
);

CREATE INDEX idx_cliente_empresa ON cliente(empresa_id);

-- Punto de entrega: un cliente puede tener varios (RF03)
CREATE TABLE punto_entrega (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    cliente_id      CHAR(36)      NOT NULL,
    direccion       VARCHAR(200)  NOT NULL,
    distrito        VARCHAR(100)  NULL,
    referencia      VARCHAR(200)  NULL,
    estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_punto_entrega_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

CREATE INDEX idx_punto_entrega_cliente ON punto_entrega(cliente_id);

-- ---------------------------------------------------------
-- 4. PRODUCTOS
-- ---------------------------------------------------------
CREATE TABLE categoria_producto (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    empresa_id      CHAR(36)      NOT NULL,
    nombre          VARCHAR(100)  NOT NULL,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_categoria_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id)
);

CREATE TABLE producto (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    empresa_id      CHAR(36)      NOT NULL,
    categoria_id    CHAR(36)      NULL,
    sku             VARCHAR(50)   NOT NULL,
    nombre          VARCHAR(150)  NOT NULL,
    descripcion     VARCHAR(300)  NULL,
    unidad_medida   VARCHAR(20)   NOT NULL,
    peso            DECIMAL(10,3) NULL,
    stock_minimo    INT           NOT NULL DEFAULT 0,
    estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_producto_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria_producto(id),
    CONSTRAINT uq_producto_empresa_sku UNIQUE (empresa_id, sku),
    CONSTRAINT chk_producto_stock_minimo CHECK (stock_minimo >= 0)
);

CREATE INDEX idx_producto_empresa ON producto(empresa_id);

-- ---------------------------------------------------------
-- 5. ALMACENES
-- ---------------------------------------------------------
CREATE TABLE almacen (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    empresa_id      CHAR(36)      NOT NULL,
    nombre          VARCHAR(100)  NOT NULL,
    direccion       VARCHAR(200)  NULL,
    estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_almacen_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id)
);

CREATE INDEX idx_almacen_empresa ON almacen(empresa_id);

CREATE TABLE ubicacion_almacen (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    almacen_id      CHAR(36)      NOT NULL,
    codigo_zona     VARCHAR(30)   NOT NULL,
    descripcion     VARCHAR(150)  NULL,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ubicacion_almacen FOREIGN KEY (almacen_id) REFERENCES almacen(id),
    CONSTRAINT uq_ubicacion_almacen_codigo UNIQUE (almacen_id, codigo_zona)
);

-- ---------------------------------------------------------
-- 6. INVENTARIO
-- stock_disponible NO se guarda: se calcula (stock_fisico - stock_reservado)
-- para evitar inconsistencias (RF06). version = control de concurrencia optimista.
-- ---------------------------------------------------------
CREATE TABLE inventario (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    producto_id     CHAR(36)      NOT NULL,
    almacen_id      CHAR(36)      NOT NULL,
    stock_fisico    INT           NOT NULL DEFAULT 0,
    stock_reservado INT           NOT NULL DEFAULT 0,
    version         INT           NOT NULL DEFAULT 0,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventario_producto FOREIGN KEY (producto_id) REFERENCES producto(id),
    CONSTRAINT fk_inventario_almacen FOREIGN KEY (almacen_id) REFERENCES almacen(id),
    CONSTRAINT uq_inventario_producto_almacen UNIQUE (producto_id, almacen_id),
    CONSTRAINT chk_inventario_fisico CHECK (stock_fisico >= 0),
    CONSTRAINT chk_inventario_reservado CHECK (stock_reservado >= 0),
    CONSTRAINT chk_inventario_reservado_no_excede CHECK (stock_reservado <= stock_fisico)
);

CREATE TABLE movimiento_inventario (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    inventario_id   CHAR(36)      NOT NULL,
    usuario_id      CHAR(36)      NULL,
    tipo            ENUM('ENTRADA','SALIDA','AJUSTE','RESERVA','LIBERACION') NOT NULL,
    cantidad        INT           NOT NULL,
    motivo          VARCHAR(200)  NULL,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movimiento_inventario FOREIGN KEY (inventario_id) REFERENCES inventario(id),
    CONSTRAINT fk_movimiento_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE INDEX idx_movimiento_inventario ON movimiento_inventario(inventario_id);

-- ---------------------------------------------------------
-- 7. PEDIDOS
-- estado controlado 100% por backend (RF08: sin saltos libres de estado)
-- ---------------------------------------------------------
CREATE TABLE pedido (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    empresa_id      CHAR(36)      NOT NULL,
    cliente_id      CHAR(36)      NOT NULL,
    punto_entrega_id CHAR(36)     NOT NULL,
    usuario_id      CHAR(36)      NOT NULL,
    estado          ENUM('CREADO','STOCK_RESERVADO','SIN_STOCK','PICKING_COMPLETADO',
                         'PACKING_COMPLETADO','EN_DISTRIBUCION','DESPACHADO','CANCELADO')
                         NOT NULL DEFAULT 'CREADO',
    prioridad       ENUM('BAJA','MEDIA','ALTA') NOT NULL DEFAULT 'MEDIA',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedido_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_pedido_punto_entrega FOREIGN KEY (punto_entrega_id) REFERENCES punto_entrega(id),
    CONSTRAINT fk_pedido_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE INDEX idx_pedido_empresa ON pedido(empresa_id);
CREATE INDEX idx_pedido_estado ON pedido(estado);

CREATE TABLE pedido_detalle (
    id              CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    pedido_id       CHAR(36)      NOT NULL,
    producto_id     CHAR(36)      NOT NULL,
    cantidad        INT           NOT NULL,
    cantidad_reservada INT        NOT NULL DEFAULT 0,
    CONSTRAINT fk_pedido_detalle_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    CONSTRAINT fk_pedido_detalle_producto FOREIGN KEY (producto_id) REFERENCES producto(id),
    CONSTRAINT uq_pedido_detalle_producto UNIQUE (pedido_id, producto_id),
    CONSTRAINT chk_pedido_detalle_cantidad CHECK (cantidad > 0)
);

CREATE INDEX idx_pedido_detalle_pedido ON pedido_detalle(pedido_id);
