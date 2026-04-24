-- =============================================================
-- Repostería Rosato - Esquema base (Módulos 1 y 2)
-- Dialecto: MySQL 8.x
-- Cubre: Autenticación (Cliente / Empleado) y Gestión de Pedidos.
-- =============================================================

DROP DATABASE IF EXISTS reposteria_rosato;
CREATE DATABASE reposteria_rosato
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE reposteria_rosato;

-- -------------------------------------------------------------
-- Módulo 1: Autenticación
-- -------------------------------------------------------------

CREATE TABLE Cliente (
    id_cliente       INT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(80)  NOT NULL,
    apellido         VARCHAR(80)  NOT NULL,
    direccion        VARCHAR(200) NOT NULL,
    telefono         VARCHAR(25)  NOT NULL,
    email            VARCHAR(120) NOT NULL UNIQUE,
    rnc_cedula       VARCHAR(25)  NULL,
    usuario          VARCHAR(40)  NOT NULL UNIQUE,
    contrasena       VARCHAR(120) NOT NULL,           -- hash BCrypt
    acepta_terminos  BOOLEAN      NOT NULL DEFAULT FALSE,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_registro   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    intentos_fallidos INT         NOT NULL DEFAULT 0,
    bloqueado_hasta  DATETIME     NULL
);

CREATE TABLE Empleado (
    id_empleado       INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo   VARCHAR(150) NOT NULL,
    cedula            VARCHAR(25)  NOT NULL UNIQUE,
    telefono          VARCHAR(25)  NOT NULL,
    usuario           VARCHAR(40)  NOT NULL UNIQUE,
    contrasena        VARCHAR(120) NOT NULL,          -- hash BCrypt
    area_trabajo      VARCHAR(60)  NOT NULL,          -- Producción, Decoración, Delivery, Limpieza, Administrador
    experiencia       VARCHAR(120) NULL,
    disponibilidad    VARCHAR(40)  NULL,              -- Completa / Medio tiempo / Fines de semana
    salario           DECIMAL(10,2) NULL,
    fecha_contratacion DATE        NULL,
    fecha_nacimiento  DATE         NULL,
    fecha_prueba_embarazo DATE     NULL,              -- condicional (solo mujeres)
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    intentos_fallidos INT          NOT NULL DEFAULT 0,
    bloqueado_hasta   DATETIME     NULL
);

-- -------------------------------------------------------------
-- Módulo 2: Pedidos
-- -------------------------------------------------------------

CREATE TABLE Pedido (
    id_pedido        INT AUTO_INCREMENT PRIMARY KEY,
    fk_id_cliente    INT          NOT NULL,
    fk_id_empleado   INT          NULL,              -- quien toma/gestiona el pedido
    fecha_pedido     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_entrega    DATETIME     NOT NULL,
    estado           VARCHAR(30)  NOT NULL DEFAULT 'Borrador',
        -- Borrador, Pendiente verificación, Confirmado, En producción,
        -- Listo para entregar, Entregado, Cancelado
    tipo_entrega     VARCHAR(20)  NOT NULL DEFAULT 'Local',  -- Local / Delivery
    subtotal         DECIMAL(12,2) NOT NULL DEFAULT 0,
    adelanto         DECIMAL(12,2) NOT NULL DEFAULT 0,
    saldo_pendiente  DECIMAL(12,2) NOT NULL DEFAULT 0,
    observaciones    TEXT         NULL,
    CONSTRAINT fk_pedido_cliente  FOREIGN KEY (fk_id_cliente)  REFERENCES Cliente(id_cliente),
    CONSTRAINT fk_pedido_empleado FOREIGN KEY (fk_id_empleado) REFERENCES Empleado(id_empleado)
);

CREATE TABLE DetallePedido (
    id_detalle        INT AUTO_INCREMENT PRIMARY KEY,
    fk_id_pedido      INT          NOT NULL,
    tipo_producto     VARCHAR(60)  NOT NULL,         -- Bizcocho, Cheesecake, Postre de copa, Galletas, etc.
    cantidad_libras   DECIMAL(5,2) NOT NULL,         -- mínimo 0.5, máximo 20
    diseno            TEXT         NOT NULL,
    diseno_complejo   BOOLEAN      NOT NULL DEFAULT FALSE,
    colaborador_externo VARCHAR(120) NULL,
    tiempo_estimado_horas DECIMAL(5,2) NULL,         -- 0.5lb=3h, 6lb=8h, intermedio proporcional
    precio_base       DECIMAL(10,2) NOT NULL DEFAULT 0,
    costo_diseno      DECIMAL(10,2) NOT NULL DEFAULT 0,
    contiene_fresas   BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (fk_id_pedido) REFERENCES Pedido(id_pedido) ON DELETE CASCADE
);

CREATE TABLE Pago (
    id_pago          INT AUTO_INCREMENT PRIMARY KEY,
    fk_id_pedido     INT          NOT NULL,
    tipo_pago        VARCHAR(30)  NOT NULL,         -- 'Adelanto 50%', 'Saldo final'
    metodo_pago      VARCHAR(30)  NOT NULL,         -- Efectivo, Tarjeta, Transferencia
    monto            DECIMAL(12,2) NOT NULL,
    referencia       VARCHAR(80)  NULL,
    fecha_pago       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    recibido_por     INT          NULL,             -- empleado que registra
    CONSTRAINT fk_pago_pedido    FOREIGN KEY (fk_id_pedido)  REFERENCES Pedido(id_pedido),
    CONSTRAINT fk_pago_empleado  FOREIGN KEY (recibido_por)  REFERENCES Empleado(id_empleado)
);

-- -------------------------------------------------------------
-- Datos semilla
-- Las contraseñas se almacenan hasheadas con BCrypt. Para crear el
-- primer empleado administrador, ejecuta la herramienta
-- `com.rosato.util.SeedAdmin` (ver README). No se insertan hashes
-- fijos aquí para evitar copiar claves inválidas.
-- -------------------------------------------------------------
