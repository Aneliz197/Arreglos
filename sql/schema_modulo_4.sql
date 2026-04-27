-- =============================================================
-- Repostería Rosato - Módulo 4: Inventario y Compras
-- Dialecto: SQL Server (T-SQL)
-- Asume que la base 'Reposteria' ya existe (schema_modulos_1_2.sql).
-- =============================================================

USE Reposteria;
GO

CREATE TABLE Ingrediente (
    id_ingrediente   INT IDENTITY(1,1) PRIMARY KEY,
    nombre           NVARCHAR(100) NOT NULL UNIQUE,
    unidad           NVARCHAR(20)  NOT NULL,                         -- lb, kg, L, unidad, etc.
    stock_actual     DECIMAL(12,3) NOT NULL CONSTRAINT DF_Ing_stock  DEFAULT (0),
    stock_minimo     DECIMAL(12,3) NOT NULL CONSTRAINT DF_Ing_min    DEFAULT (0),
    costo_promedio   DECIMAL(12,4) NOT NULL CONSTRAINT DF_Ing_costo  DEFAULT (0),  -- RD$ por unidad
    activo           BIT           NOT NULL CONSTRAINT DF_Ing_activo DEFAULT (1),
    fecha_registro   DATETIME2     NOT NULL CONSTRAINT DF_Ing_fecha  DEFAULT (SYSDATETIME())
);
GO

CREATE TABLE Proveedor (
    id_proveedor     INT IDENTITY(1,1) PRIMARY KEY,
    nombre           NVARCHAR(150) NOT NULL,
    rnc              NVARCHAR(30)  NULL,
    telefono         NVARCHAR(25)  NOT NULL,
    email            NVARCHAR(120) NULL,
    direccion        NVARCHAR(200) NULL,
    activo           BIT           NOT NULL CONSTRAINT DF_Prov_activo DEFAULT (1),
    fecha_registro   DATETIME2     NOT NULL CONSTRAINT DF_Prov_fecha  DEFAULT (SYSDATETIME())
);
GO

CREATE TABLE Compra (
    id_compra        INT IDENTITY(1,1) PRIMARY KEY,
    fk_id_proveedor  INT           NOT NULL,
    fk_id_empleado   INT           NULL,                             -- empleado que registra la compra
    fecha_compra     DATETIME2     NOT NULL CONSTRAINT DF_Comp_fecha  DEFAULT (SYSDATETIME()),
    numero_factura   NVARCHAR(50)  NULL,
    total            DECIMAL(12,2) NOT NULL CONSTRAINT DF_Comp_total  DEFAULT (0),
    observaciones    NVARCHAR(MAX) NULL,
    CONSTRAINT fk_compra_proveedor FOREIGN KEY (fk_id_proveedor) REFERENCES Proveedor(id_proveedor),
    CONSTRAINT fk_compra_empleado  FOREIGN KEY (fk_id_empleado)  REFERENCES Empleado(id_empleado)
);
GO

CREATE TABLE DetalleCompra (
    id_detalle       INT IDENTITY(1,1) PRIMARY KEY,
    fk_id_compra     INT           NOT NULL,
    fk_id_ingrediente INT          NOT NULL,
    cantidad         DECIMAL(12,3) NOT NULL,
    precio_unitario  DECIMAL(12,4) NOT NULL,
    subtotal         DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_det_compra  FOREIGN KEY (fk_id_compra)     REFERENCES Compra(id_compra) ON DELETE CASCADE,
    CONSTRAINT fk_det_ing     FOREIGN KEY (fk_id_ingrediente) REFERENCES Ingrediente(id_ingrediente)
);
GO
