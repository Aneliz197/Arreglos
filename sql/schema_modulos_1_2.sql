-- =============================================================
-- Repostería Rosato - Esquema base (Módulos 1 y 2)
-- Dialecto: SQL Server (T-SQL)
-- Cubre: Autenticación (Cliente / Empleado) y Gestión de Pedidos.
-- =============================================================

IF DB_ID('Reposteria') IS NULL
    CREATE DATABASE Reposteria;
GO

USE Reposteria;
GO

-- -------------------------------------------------------------
-- Módulo 1: Autenticación
-- -------------------------------------------------------------

CREATE TABLE Cliente (
    id_cliente        INT IDENTITY(1,1) PRIMARY KEY,
    nombre            NVARCHAR(80)  NOT NULL,
    apellido          NVARCHAR(80)  NOT NULL,
    direccion         NVARCHAR(200) NOT NULL,
    telefono          NVARCHAR(25)  NOT NULL,
    email             NVARCHAR(120) NOT NULL UNIQUE,
    rnc_cedula        NVARCHAR(25)  NULL,
    usuario           NVARCHAR(40)  NOT NULL UNIQUE,
    contrasena        NVARCHAR(120) NOT NULL,                     -- hash BCrypt
    acepta_terminos   BIT           NOT NULL CONSTRAINT DF_Cliente_aceptaTerminos  DEFAULT (0),
    activo            BIT           NOT NULL CONSTRAINT DF_Cliente_activo          DEFAULT (1),
    fecha_registro    DATETIME2     NOT NULL CONSTRAINT DF_Cliente_fechaRegistro   DEFAULT (SYSDATETIME()),
    intentos_fallidos INT           NOT NULL CONSTRAINT DF_Cliente_intentos        DEFAULT (0),
    bloqueado_hasta   DATETIME2     NULL
);
GO

CREATE TABLE Empleado (
    id_empleado           INT IDENTITY(1,1) PRIMARY KEY,
    nombre_completo       NVARCHAR(150) NOT NULL,
    cedula                NVARCHAR(25)  NOT NULL UNIQUE,
    telefono              NVARCHAR(25)  NOT NULL,
    usuario               NVARCHAR(40)  NOT NULL UNIQUE,
    contrasena            NVARCHAR(120) NOT NULL,                 -- hash BCrypt
    area_trabajo          NVARCHAR(60)  NOT NULL,                 -- Producción, Decoración, Delivery, Limpieza, Administrador
    experiencia           NVARCHAR(120) NULL,
    disponibilidad        NVARCHAR(40)  NULL,                     -- Completa / Medio tiempo / Fines de semana
    salario               DECIMAL(10,2) NULL,
    fecha_contratacion    DATE          NULL,
    fecha_nacimiento      DATE          NULL,
    fecha_prueba_embarazo DATE          NULL,                     -- condicional (solo mujeres)
    activo                BIT           NOT NULL CONSTRAINT DF_Empleado_activo    DEFAULT (1),
    intentos_fallidos     INT           NOT NULL CONSTRAINT DF_Empleado_intentos  DEFAULT (0),
    bloqueado_hasta       DATETIME2     NULL
);
GO

-- -------------------------------------------------------------
-- Módulo 2: Pedidos
-- -------------------------------------------------------------

CREATE TABLE Pedido (
    id_pedido        INT IDENTITY(1,1) PRIMARY KEY,
    fk_id_cliente    INT           NOT NULL,
    fk_id_empleado   INT           NULL,                          -- quien toma/gestiona el pedido
    fecha_pedido     DATETIME2     NOT NULL CONSTRAINT DF_Pedido_fechaPedido DEFAULT (SYSDATETIME()),
    fecha_entrega    DATETIME2     NOT NULL,
    estado           NVARCHAR(30)  NOT NULL CONSTRAINT DF_Pedido_estado      DEFAULT ('Borrador'),
        -- Borrador, Pendiente verificación, Confirmado, En producción,
        -- Listo para entregar, Entregado, Cancelado
    tipo_entrega     NVARCHAR(20)  NOT NULL CONSTRAINT DF_Pedido_tipoEntrega DEFAULT ('Local'), -- Local / Delivery
    subtotal         DECIMAL(12,2) NOT NULL CONSTRAINT DF_Pedido_subtotal    DEFAULT (0),
    adelanto         DECIMAL(12,2) NOT NULL CONSTRAINT DF_Pedido_adelanto    DEFAULT (0),
    saldo_pendiente  DECIMAL(12,2) NOT NULL CONSTRAINT DF_Pedido_saldo       DEFAULT (0),
    observaciones    NVARCHAR(MAX) NULL,
    CONSTRAINT fk_pedido_cliente  FOREIGN KEY (fk_id_cliente)  REFERENCES Cliente(id_cliente),
    CONSTRAINT fk_pedido_empleado FOREIGN KEY (fk_id_empleado) REFERENCES Empleado(id_empleado)
);
GO

CREATE TABLE DetallePedido (
    id_detalle             INT IDENTITY(1,1) PRIMARY KEY,
    fk_id_pedido           INT           NOT NULL,
    tipo_producto          NVARCHAR(60)  NOT NULL,                -- Bizcocho, Cheesecake, etc.
    cantidad_libras        DECIMAL(5,2)  NOT NULL,                -- mínimo 0.5, máximo 20
    diseno                 NVARCHAR(MAX) NOT NULL,
    diseno_complejo        BIT           NOT NULL CONSTRAINT DF_DetPed_complejo DEFAULT (0),
    colaborador_externo    NVARCHAR(120) NULL,
    tiempo_estimado_horas  DECIMAL(5,2)  NULL,                    -- 0.5lb=3h, 6lb=8h, interpolado
    precio_base            DECIMAL(10,2) NOT NULL CONSTRAINT DF_DetPed_precio   DEFAULT (0),
    costo_diseno           DECIMAL(10,2) NOT NULL CONSTRAINT DF_DetPed_costoD   DEFAULT (0),
    contiene_fresas        BIT           NOT NULL CONSTRAINT DF_DetPed_fresas   DEFAULT (0),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (fk_id_pedido)
        REFERENCES Pedido(id_pedido) ON DELETE CASCADE
);
GO

CREATE TABLE Pago (
    id_pago       INT IDENTITY(1,1) PRIMARY KEY,
    fk_id_pedido  INT           NOT NULL,
    tipo_pago     NVARCHAR(30)  NOT NULL,                         -- 'Adelanto 50%', 'Saldo final'
    metodo_pago   NVARCHAR(30)  NOT NULL,                         -- Efectivo, Tarjeta, Transferencia
    monto         DECIMAL(12,2) NOT NULL,
    referencia    NVARCHAR(80)  NULL,
    fecha_pago    DATETIME2     NOT NULL CONSTRAINT DF_Pago_fecha DEFAULT (SYSDATETIME()),
    recibido_por  INT           NULL,                             -- empleado que registra
    CONSTRAINT fk_pago_pedido    FOREIGN KEY (fk_id_pedido) REFERENCES Pedido(id_pedido),
    CONSTRAINT fk_pago_empleado  FOREIGN KEY (recibido_por) REFERENCES Empleado(id_empleado)
);
GO

-- -------------------------------------------------------------
-- Datos semilla
-- Las contraseñas se almacenan hasheadas con BCrypt. Para crear
-- el primer empleado administrador, ejecuta la herramienta
-- `com.rosato.util.SeedAdmin` (ver README).
-- -------------------------------------------------------------
