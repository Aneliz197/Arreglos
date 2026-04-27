-- =============================================================
-- Repostería Rosato - Módulo 7: Empleados y Capacitación
-- Dialecto: SQL Server (T-SQL)
-- Asume que la base y los módulos 1-2 ya fueron aplicados.
-- El alta/edición del propio Empleado usa la tabla ya existente.
-- =============================================================

USE Reposteria;
GO

CREATE TABLE Capacitacion (
    id_capacitacion   INT IDENTITY(1,1) PRIMARY KEY,
    titulo            NVARCHAR(150) NOT NULL,
    fecha             DATE          NOT NULL,
    instructor        NVARCHAR(120) NULL,
    horas             DECIMAL(6,2)  NOT NULL CONSTRAINT DF_Cap_horas DEFAULT (0),
    ubicacion         NVARCHAR(120) NULL,
    descripcion       NVARCHAR(MAX) NULL,
    fecha_registro    DATETIME2     NOT NULL CONSTRAINT DF_Cap_fecha DEFAULT (SYSDATETIME())
);
GO

CREATE TABLE CapacitacionEmpleado (
    id_capacitacion_emp  INT IDENTITY(1,1) PRIMARY KEY,
    fk_id_capacitacion   INT           NOT NULL,
    fk_id_empleado       INT           NOT NULL,
    asistio              BIT           NOT NULL CONSTRAINT DF_CapE_asistio DEFAULT (0),
    calificacion         DECIMAL(5,2)  NULL,
    observaciones        NVARCHAR(300) NULL,
    CONSTRAINT fk_cape_cap FOREIGN KEY (fk_id_capacitacion) REFERENCES Capacitacion(id_capacitacion) ON DELETE CASCADE,
    CONSTRAINT fk_cape_emp FOREIGN KEY (fk_id_empleado)     REFERENCES Empleado(id_empleado),
    CONSTRAINT uq_cap_emp  UNIQUE (fk_id_capacitacion, fk_id_empleado)
);
GO
