-- =============================================================
-- Repostería Rosato - Módulo 8: Higiene / Control sanitario
-- Dialecto: SQL Server (T-SQL)
-- Asume que la base y los módulos 1-2 ya fueron aplicados.
-- =============================================================

USE Reposteria;
GO

CREATE TABLE RevisionHigiene (
    id_revision       INT IDENTITY(1,1) PRIMARY KEY,
    fecha             DATE          NOT NULL,
    area              NVARCHAR(80)  NOT NULL,
    responsable       NVARCHAR(120) NULL,
    fk_id_empleado    INT           NULL,
    puntaje           DECIMAL(5,2)  NOT NULL CONSTRAINT DF_RevH_puntaje DEFAULT (0),
    estado            NVARCHAR(20)  NOT NULL CONSTRAINT DF_RevH_estado  DEFAULT ('Observado'),
    observaciones     NVARCHAR(MAX) NULL,
    fecha_registro    DATETIME2     NOT NULL CONSTRAINT DF_RevH_fecha   DEFAULT (SYSDATETIME()),
    CONSTRAINT fk_revh_empleado FOREIGN KEY (fk_id_empleado) REFERENCES Empleado(id_empleado)
);
GO

CREATE TABLE RevisionHigieneItem (
    id_item           INT IDENTITY(1,1) PRIMARY KEY,
    fk_id_revision    INT           NOT NULL,
    descripcion       NVARCHAR(200) NOT NULL,
    cumple            BIT           NOT NULL CONSTRAINT DF_RevHI_cumple DEFAULT (0),
    observacion       NVARCHAR(300) NULL,
    CONSTRAINT fk_revhi_rev FOREIGN KEY (fk_id_revision) REFERENCES RevisionHigiene(id_revision) ON DELETE CASCADE
);
GO

CREATE TABLE ControlSanitarioEmpleado (
    id_control        INT IDENTITY(1,1) PRIMARY KEY,
    fecha             DATE          NOT NULL,
    fk_id_empleado    INT           NOT NULL,
    uniforme_ok       BIT           NOT NULL CONSTRAINT DF_CS_uni   DEFAULT (0),
    manos_ok          BIT           NOT NULL CONSTRAINT DF_CS_man   DEFAULT (0),
    redecilla_ok      BIT           NOT NULL CONSTRAINT DF_CS_red   DEFAULT (0),
    guantes_ok        BIT           NOT NULL CONSTRAINT DF_CS_gua   DEFAULT (0),
    unas_ok           BIT           NOT NULL CONSTRAINT DF_CS_unas  DEFAULT (0),
    salud_ok          BIT           NOT NULL CONSTRAINT DF_CS_sal   DEFAULT (0),
    observaciones     NVARCHAR(300) NULL,
    fecha_registro    DATETIME2     NOT NULL CONSTRAINT DF_CS_fecha DEFAULT (SYSDATETIME()),
    CONSTRAINT fk_cs_empleado FOREIGN KEY (fk_id_empleado) REFERENCES Empleado(id_empleado),
    CONSTRAINT uq_cs_fecha_emp UNIQUE (fecha, fk_id_empleado)
);
GO
