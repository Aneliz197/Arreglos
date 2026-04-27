-- =============================================================
-- Repostería Rosato - Módulo 6: Máquinas y Mantenimiento
-- Dialecto: SQL Server (T-SQL)
-- Asume que la base y los módulos 1-2 ya fueron aplicados.
-- =============================================================

USE Reposteria;
GO

CREATE TABLE Maquina (
    id_maquina        INT IDENTITY(1,1) PRIMARY KEY,
    codigo            NVARCHAR(30)  NOT NULL UNIQUE,
    nombre            NVARCHAR(100) NOT NULL,
    tipo              NVARCHAR(50)  NOT NULL,                              -- Horno, Batidora, Refrigerador, etc.
    marca             NVARCHAR(80)  NULL,
    modelo            NVARCHAR(80)  NULL,
    serial            NVARCHAR(80)  NULL,
    fecha_adquisicion DATE          NULL,
    estado            NVARCHAR(30)  NOT NULL CONSTRAINT DF_Maq_estado DEFAULT ('Operativa'),
                                                                           -- Operativa / En mantenimiento / Fuera de servicio
    proxima_revision  DATE          NULL,
    ubicacion         NVARCHAR(120) NULL,
    notas             NVARCHAR(MAX) NULL,
    activo            BIT           NOT NULL CONSTRAINT DF_Maq_activo DEFAULT (1),
    fecha_registro    DATETIME2     NOT NULL CONSTRAINT DF_Maq_fecha  DEFAULT (SYSDATETIME())
);
GO

CREATE TABLE Mantenimiento (
    id_mantenimiento  INT IDENTITY(1,1) PRIMARY KEY,
    fk_id_maquina     INT           NOT NULL,
    tipo              NVARCHAR(20)  NOT NULL,                              -- Preventivo / Correctivo
    fecha             DATE          NOT NULL,
    tecnico           NVARCHAR(120) NULL,
    costo             DECIMAL(12,2) NOT NULL CONSTRAINT DF_Mant_costo DEFAULT (0),
    descripcion       NVARCHAR(MAX) NULL,
    proxima_revision  DATE          NULL,
    fk_id_empleado    INT           NULL,                                  -- quien registra
    fecha_registro    DATETIME2     NOT NULL CONSTRAINT DF_Mant_fecha DEFAULT (SYSDATETIME()),
    CONSTRAINT fk_mant_maquina   FOREIGN KEY (fk_id_maquina)  REFERENCES Maquina(id_maquina) ON DELETE CASCADE,
    CONSTRAINT fk_mant_empleado  FOREIGN KEY (fk_id_empleado) REFERENCES Empleado(id_empleado)
);
GO
