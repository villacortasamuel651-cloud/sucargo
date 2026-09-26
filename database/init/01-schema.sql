-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: Sucargo
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `almacen`
--

DROP TABLE IF EXISTS `almacen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `almacen` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `empresa_id` char(36) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `estado` enum('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_almacen_empresa` (`empresa_id`),
  CONSTRAINT `fk_almacen_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresa` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bulto`
--

DROP TABLE IF EXISTS `bulto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bulto` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `pedido_id` char(36) NOT NULL,
  `codigo` varchar(20) DEFAULT NULL,
  `numero` int NOT NULL,
  `peso` decimal(10,3) DEFAULT NULL,
  `estado` enum('ABIERTO','CERRADO') NOT NULL DEFAULT 'ABIERTO',
  `usuario_id` char(36) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_bulto_pedido_numero` (`pedido_id`,`numero`),
  KEY `fk_bulto_usuario` (`usuario_id`),
  KEY `idx_bulto_pedido` (`pedido_id`),
  CONSTRAINT `fk_bulto_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`),
  CONSTRAINT `fk_bulto_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bulto_detalle`
--

DROP TABLE IF EXISTS `bulto_detalle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bulto_detalle` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `bulto_id` char(36) NOT NULL,
  `producto_id` char(36) NOT NULL,
  `cantidad` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_bulto_detalle_producto` (`producto_id`),
  KEY `idx_bulto_detalle_bulto` (`bulto_id`),
  CONSTRAINT `fk_bulto_detalle_bulto` FOREIGN KEY (`bulto_id`) REFERENCES `bulto` (`id`),
  CONSTRAINT `fk_bulto_detalle_producto` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`id`),
  CONSTRAINT `chk_bulto_detalle_cantidad` CHECK ((`cantidad` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `categoria_producto`
--

DROP TABLE IF EXISTS `categoria_producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria_producto` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `empresa_id` char(36) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_categoria_empresa` (`empresa_id`),
  CONSTRAINT `fk_categoria_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresa` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cliente`
--

DROP TABLE IF EXISTS `cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cliente` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `empresa_id` char(36) NOT NULL,
  `razon_social` varchar(150) NOT NULL,
  `ruc_dni` varchar(11) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `estado` enum('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_cliente_empresa_ruc` (`empresa_id`,`ruc_dni`),
  KEY `idx_cliente_empresa` (`empresa_id`),
  CONSTRAINT `fk_cliente_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresa` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `despacho`
--

DROP TABLE IF EXISTS `despacho`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `despacho` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `pedido_id` char(36) NOT NULL,
  `orden_transporte_id` char(36) DEFAULT NULL,
  `usuario_id` char(36) DEFAULT NULL,
  `estado` enum('EN_VERIFICACION','DESPACHADO','COMPLETADO','CANCELADO') NOT NULL DEFAULT 'EN_VERIFICACION',
  `fecha_inicio` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_completado` datetime DEFAULT NULL,
  `fecha_despacho` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_despacho_pedido` (`pedido_id`),
  KEY `fk_despacho_ot` (`orden_transporte_id`),
  KEY `fk_despacho_usuario` (`usuario_id`),
  CONSTRAINT `fk_despacho_ot` FOREIGN KEY (`orden_transporte_id`) REFERENCES `orden_transporte` (`id`),
  CONSTRAINT `fk_despacho_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`),
  CONSTRAINT `fk_despacho_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `despacho_bulto_verificado`
--

DROP TABLE IF EXISTS `despacho_bulto_verificado`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `despacho_bulto_verificado` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `despacho_id` char(36) NOT NULL,
  `bulto_id` char(36) NOT NULL,
  `verificado` tinyint(1) NOT NULL DEFAULT '0',
  `fecha_verificacion` datetime DEFAULT NULL,
  `verificado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_despacho_bulto` (`despacho_id`,`bulto_id`),
  KEY `fk_dbv_bulto` (`bulto_id`),
  CONSTRAINT `fk_dbv_bulto` FOREIGN KEY (`bulto_id`) REFERENCES `bulto` (`id`),
  CONSTRAINT `fk_dbv_despacho` FOREIGN KEY (`despacho_id`) REFERENCES `despacho` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `distribucion`
--

DROP TABLE IF EXISTS `distribucion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `distribucion` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `empresa_id` char(36) NOT NULL,
  `transportista_id` char(36) NOT NULL,
  `codigo` varchar(20) DEFAULT NULL,
  `fecha` date NOT NULL,
  `estado` enum('ABIERTA','CONFIRMADA','CANCELADA') NOT NULL DEFAULT 'ABIERTA',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_distribucion_transportista` (`transportista_id`),
  KEY `idx_distribucion_empresa` (`empresa_id`),
  KEY `idx_distribucion_fecha` (`fecha`),
  CONSTRAINT `fk_distribucion_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresa` (`id`),
  CONSTRAINT `fk_distribucion_transportista` FOREIGN KEY (`transportista_id`) REFERENCES `transportista` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `distribucion_pedido`
--

DROP TABLE IF EXISTS `distribucion_pedido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `distribucion_pedido` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `distribucion_id` char(36) NOT NULL,
  `pedido_id` char(36) NOT NULL,
  `secuencia` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_distribucion_pedido` (`distribucion_id`,`pedido_id`),
  UNIQUE KEY `uq_distribucion_secuencia` (`distribucion_id`,`secuencia`),
  KEY `fk_distribucion_pedido_pedido` (`pedido_id`),
  CONSTRAINT `fk_distribucion_pedido_distribucion` FOREIGN KEY (`distribucion_id`) REFERENCES `distribucion` (`id`),
  CONSTRAINT `fk_distribucion_pedido_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `empresa`
--

DROP TABLE IF EXISTS `empresa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `empresa` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `ruc` varchar(11) NOT NULL,
  `razon_social` varchar(150) NOT NULL,
  `rubro` varchar(100) DEFAULT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `representante_legal` varchar(150) DEFAULT NULL,
  `estado` enum('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ruc` (`ruc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inventario`
--

DROP TABLE IF EXISTS `inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventario` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `producto_id` char(36) NOT NULL,
  `almacen_id` char(36) NOT NULL,
  `stock_fisico` int NOT NULL DEFAULT '0',
  `stock_reservado` int NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_inventario_producto_almacen` (`producto_id`,`almacen_id`),
  KEY `idx_inventario_producto` (`producto_id`),
  KEY `fk_inventario_almacen` (`almacen_id`),
  CONSTRAINT `fk_inventario_almacen` FOREIGN KEY (`almacen_id`) REFERENCES `almacen` (`id`),
  CONSTRAINT `fk_inventario_producto` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`id`),
  CONSTRAINT `chk_inventario_fisico` CHECK ((`stock_fisico` >= 0)),
  CONSTRAINT `chk_inventario_reservado` CHECK ((`stock_reservado` >= 0)),
  CONSTRAINT `chk_inventario_reservado_no_excede` CHECK ((`stock_reservado` <= `stock_fisico`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `movimiento_inventario`
--

DROP TABLE IF EXISTS `movimiento_inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimiento_inventario` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `inventario_id` char(36) NOT NULL,
  `usuario_id` char(36) DEFAULT NULL,
  `tipo` enum('ENTRADA','SALIDA','AJUSTE','RESERVA','LIBERACION') NOT NULL,
  `cantidad` int NOT NULL,
  `motivo` varchar(200) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_movimiento_usuario` (`usuario_id`),
  KEY `idx_movimiento_inventario` (`inventario_id`),
  CONSTRAINT `fk_movimiento_inventario` FOREIGN KEY (`inventario_id`) REFERENCES `inventario` (`id`),
  CONSTRAINT `fk_movimiento_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `orden_transporte`
--

DROP TABLE IF EXISTS `orden_transporte`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orden_transporte` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `distribucion_id` char(36) NOT NULL,
  `pedido_id` char(36) NOT NULL,
  `numero` varchar(20) DEFAULT NULL,
  `origen_almacen_id` char(36) DEFAULT NULL,
  `destino_punto_entrega_id` char(36) NOT NULL,
  `total_bultos` int NOT NULL,
  `peso_total` decimal(10,3) DEFAULT NULL,
  `ventana_inicio` time DEFAULT NULL,
  `ventana_fin` time DEFAULT NULL,
  `estado` enum('EMITIDA','ANULADA') NOT NULL DEFAULT 'EMITIDA',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ot_pedido` (`pedido_id`),
  KEY `fk_ot_almacen` (`origen_almacen_id`),
  KEY `fk_ot_punto_entrega` (`destino_punto_entrega_id`),
  KEY `idx_ot_distribucion` (`distribucion_id`),
  CONSTRAINT `fk_ot_almacen` FOREIGN KEY (`origen_almacen_id`) REFERENCES `almacen` (`id`),
  CONSTRAINT `fk_ot_distribucion` FOREIGN KEY (`distribucion_id`) REFERENCES `distribucion` (`id`),
  CONSTRAINT `fk_ot_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`),
  CONSTRAINT `fk_ot_punto_entrega` FOREIGN KEY (`destino_punto_entrega_id`) REFERENCES `punto_entrega` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pedido`
--

DROP TABLE IF EXISTS `pedido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedido` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `empresa_id` char(36) NOT NULL,
  `cliente_id` char(36) NOT NULL,
  `punto_entrega_id` char(36) NOT NULL,
  `usuario_id` char(36) NOT NULL,
  `almacen_id` char(36) NOT NULL,
  `estado` enum('CREADO','CONFIRMADO','STOCK_RESERVADO','SIN_STOCK','EN_PREPARACION','PICKING_COMPLETADO','PACKING_COMPLETADO','LISTO_PARA_DESPACHO','DESPACHADO','CANCELADO') NOT NULL DEFAULT 'CREADO',
  `prioridad` enum('BAJA','MEDIA','ALTA') NOT NULL DEFAULT 'MEDIA',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `codigo` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_pedido_cliente` (`cliente_id`),
  KEY `fk_pedido_punto_entrega` (`punto_entrega_id`),
  KEY `fk_pedido_usuario` (`usuario_id`),
  KEY `idx_pedido_empresa` (`empresa_id`),
  KEY `idx_pedido_estado` (`estado`),
  KEY `fk_pedido_almacen` (`almacen_id`),
  CONSTRAINT `fk_pedido_almacen` FOREIGN KEY (`almacen_id`) REFERENCES `almacen` (`id`),
  CONSTRAINT `fk_pedido_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`),
  CONSTRAINT `fk_pedido_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresa` (`id`),
  CONSTRAINT `fk_pedido_punto_entrega` FOREIGN KEY (`punto_entrega_id`) REFERENCES `punto_entrega` (`id`),
  CONSTRAINT `fk_pedido_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pedido_detalle`
--

DROP TABLE IF EXISTS `pedido_detalle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedido_detalle` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `pedido_id` char(36) NOT NULL,
  `producto_id` char(36) NOT NULL,
  `cantidad` int NOT NULL,
  `cantidad_reservada` int NOT NULL DEFAULT '0',
  `ubicacion_reservada_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pedido_detalle_producto` (`pedido_id`,`producto_id`),
  KEY `fk_pedido_detalle_producto` (`producto_id`),
  KEY `idx_pedido_detalle_pedido` (`pedido_id`),
  CONSTRAINT `fk_pedido_detalle_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`),
  CONSTRAINT `fk_pedido_detalle_producto` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`id`),
  CONSTRAINT `chk_pedido_detalle_cantidad` CHECK ((`cantidad` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `picking`
--

DROP TABLE IF EXISTS `picking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `picking` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `pedido_id` char(36) NOT NULL,
  `usuario_id` char(36) DEFAULT NULL,
  `estado` enum('PENDIENTE','EN_PROCESO','COMPLETADO','COMPLETADO_CON_INCIDENCIAS') NOT NULL DEFAULT 'PENDIENTE',
  `fecha_inicio` datetime DEFAULT NULL,
  `fecha_fin` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_picking_pedido` (`pedido_id`),
  KEY `fk_picking_usuario` (`usuario_id`),
  CONSTRAINT `fk_picking_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`),
  CONSTRAINT `fk_picking_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `picking_detalle`
--

DROP TABLE IF EXISTS `picking_detalle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `picking_detalle` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `picking_id` char(36) NOT NULL,
  `pedido_detalle_id` char(36) NOT NULL,
  `cantidad_esperada` int NOT NULL,
  `cantidad_recolectada` int NOT NULL DEFAULT '0',
  `incidencia` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_picking_detalle_pedido_detalle` (`pedido_detalle_id`),
  KEY `idx_picking_detalle_picking` (`picking_id`),
  CONSTRAINT `fk_picking_detalle_pedido_detalle` FOREIGN KEY (`pedido_detalle_id`) REFERENCES `pedido_detalle` (`id`),
  CONSTRAINT `fk_picking_detalle_picking` FOREIGN KEY (`picking_id`) REFERENCES `picking` (`id`),
  CONSTRAINT `chk_picking_detalle_cantidades` CHECK (((`cantidad_esperada` > 0) and (`cantidad_recolectada` >= 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `producto`
--

DROP TABLE IF EXISTS `producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `empresa_id` char(36) NOT NULL,
  `categoria_id` char(36) DEFAULT NULL,
  `sku` varchar(50) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `descripcion` varchar(300) DEFAULT NULL,
  `unidad_medida` varchar(20) NOT NULL,
  `peso` decimal(10,3) DEFAULT NULL,
  `stock_minimo` int NOT NULL DEFAULT '0',
  `estado` enum('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_producto_empresa_sku` (`empresa_id`,`sku`),
  KEY `fk_producto_categoria` (`categoria_id`),
  KEY `idx_producto_empresa` (`empresa_id`),
  CONSTRAINT `fk_producto_categoria` FOREIGN KEY (`categoria_id`) REFERENCES `categoria_producto` (`id`),
  CONSTRAINT `fk_producto_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresa` (`id`),
  CONSTRAINT `chk_producto_stock_minimo` CHECK ((`stock_minimo` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `punto_entrega`
--

DROP TABLE IF EXISTS `punto_entrega`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `punto_entrega` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `cliente_id` char(36) NOT NULL,
  `direccion` varchar(200) NOT NULL,
  `distrito` varchar(100) DEFAULT NULL,
  `referencia` varchar(200) DEFAULT NULL,
  `estado` enum('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_punto_entrega_cliente` (`cliente_id`),
  CONSTRAINT `fk_punto_entrega_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `refresh_token`
--

DROP TABLE IF EXISTS `refresh_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_token` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `usuario_id` char(36) NOT NULL,
  `token` varchar(255) NOT NULL,
  `expires_at` datetime NOT NULL,
  `revocado` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `idx_refresh_token_usuario` (`usuario_id`),
  CONSTRAINT `fk_refresh_token_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `transportista`
--

DROP TABLE IF EXISTS `transportista`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transportista` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `empresa_id` char(36) NOT NULL,
  `razon_social` varchar(150) NOT NULL,
  `ruc` varchar(11) DEFAULT NULL,
  `contacto` varchar(150) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `correo` varchar(150) DEFAULT NULL,
  `tipo_servicio` varchar(50) DEFAULT NULL,
  `estado` enum('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_transportista_empresa` (`empresa_id`),
  CONSTRAINT `fk_transportista_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresa` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ubicacion_almacen`
--

DROP TABLE IF EXISTS `ubicacion_almacen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ubicacion_almacen` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `almacen_id` char(36) NOT NULL,
  `codigo_zona` varchar(30) NOT NULL,
  `descripcion` varchar(150) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ubicacion_almacen_codigo` (`almacen_id`,`codigo_zona`),
  CONSTRAINT `fk_ubicacion_almacen` FOREIGN KEY (`almacen_id`) REFERENCES `almacen` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `empresa_id` char(36) DEFAULT NULL,
  `nombre` varchar(150) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `rol` enum('ADMIN_SUC','ADMIN_EMPRESA','VENTAS','ALMACENERO','SUPERVISOR_ALMACEN','LOGISTICA','DESPACHO') NOT NULL,
  `estado` enum('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  `ultimo_login` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_usuario_empresa` (`empresa_id`),
  CONSTRAINT `fk_usuario_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresa` (`id`),
  CONSTRAINT `chk_usuario_admin_suc_sin_empresa` CHECK ((((`rol` = _utf8mb4'ADMIN_SUC') and (`empresa_id` is null)) or ((`rol` <> _utf8mb4'ADMIN_SUC') and (`empresa_id` is not null))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'Sucargo'
--

--
-- Dumping routines for database 'Sucargo'
--
/*!50003 DROP PROCEDURE IF EXISTS `sp_registrar_movimiento_inventario` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `sp_registrar_movimiento_inventario`(
    IN p_producto_id       CHAR(36),
    IN p_almacen_id        CHAR(36),
    IN p_tipo              VARCHAR(20),
    IN p_cantidad          INT,
    IN p_usuario_id        CHAR(36),
    IN p_motivo            VARCHAR(200),
    OUT p_inventario_id    CHAR(36),
    OUT p_stock_fisico     INT,
    OUT p_stock_reservado  INT
)
BEGIN
    DECLARE v_inventario_id CHAR(36);
    DECLARE v_stock_fisico INT;
    DECLARE v_stock_reservado INT;
    DECLARE v_disponible INT;

    IF p_cantidad <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La cantidad debe ser mayor que cero';
    END IF;

    START TRANSACTION;

    SELECT id, stock_fisico, stock_reservado
        INTO v_inventario_id, v_stock_fisico, v_stock_reservado
        FROM inventario
        WHERE producto_id = p_producto_id
          AND almacen_id = p_almacen_id
        FOR UPDATE;

    IF v_inventario_id IS NULL THEN

        IF p_tipo <> 'ENTRADA' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT =
                    'No existe inventario para este producto/almacén';
        END IF;

        SET v_inventario_id = UUID();

        INSERT INTO inventario (
            id,
            producto_id,
            almacen_id,
            stock_fisico,
            stock_reservado
        )
        VALUES (
            v_inventario_id,
            p_producto_id,
            p_almacen_id,
            0,
            0
        );

        SET v_stock_fisico = 0;
        SET v_stock_reservado = 0;
    END IF;

    SET v_disponible = v_stock_fisico - v_stock_reservado;

    CASE p_tipo

        WHEN 'ENTRADA' THEN

            SET v_stock_fisico = v_stock_fisico + p_cantidad;

        WHEN 'SALIDA' THEN

            IF v_disponible < p_cantidad THEN
                SIGNAL SQLSTATE '45000'
                    SET MESSAGE_TEXT =
                        'Stock disponible insuficiente para la salida';
            END IF;

            SET v_stock_fisico = v_stock_fisico - p_cantidad;

        WHEN 'RESERVA' THEN

            IF v_disponible < p_cantidad THEN
                SIGNAL SQLSTATE '45000'
                    SET MESSAGE_TEXT =
                        'Stock disponible insuficiente para reservar';
            END IF;

            SET v_stock_reservado = v_stock_reservado + p_cantidad;

        WHEN 'LIBERACION' THEN

            IF v_stock_reservado < p_cantidad THEN
                SIGNAL SQLSTATE '45000'
                    SET MESSAGE_TEXT =
                        'No hay esa cantidad reservada para liberar';
            END IF;

            SET v_stock_reservado = v_stock_reservado - p_cantidad;

        WHEN 'AJUSTE' THEN

            IF p_cantidad < v_stock_reservado THEN
                SIGNAL SQLSTATE '45000'
                    SET MESSAGE_TEXT =
                        'El nuevo stock físico no puede ser menor que el stock reservado';
            END IF;

            SET v_stock_fisico = p_cantidad;

        ELSE

            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT =
                    'Tipo de movimiento no reconocido';

    END CASE;

    UPDATE inventario
        SET stock_fisico = v_stock_fisico,
            stock_reservado = v_stock_reservado,
            version = version + 1
        WHERE id = v_inventario_id;

    INSERT INTO movimiento_inventario (
        id,
        inventario_id,
        usuario_id,
        tipo,
        cantidad,
        motivo
    )
    VALUES (
        UUID(),
        v_inventario_id,
        p_usuario_id,
        p_tipo,
        p_cantidad,
        p_motivo
    );

    COMMIT;

    SET p_inventario_id = v_inventario_id;
    SET p_stock_fisico = v_stock_fisico;
    SET p_stock_reservado = v_stock_reservado;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-24 16:47:19
