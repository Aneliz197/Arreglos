-- =============================================================
-- Repostería Rosato - Módulo 5: Entregas y Cobros
-- Dialecto: SQL Server (T-SQL)
-- Asume que la base y los módulos 1-2 ya fueron aplicados.
-- =============================================================

USE Reposteria;
GO

CREATE TABLE Entrega (
    id_entrega        INT IDENTITY(1,1) PRIMARY KEY,
    fk_id_pedido      INT           NOT NULL UNIQUE,
    tipo_entrega      NVARCHAR(20)  NOT NULL,                         -- 'Local' / 'Domicilio'
    direccion         NVARCHAR(300) NULL,
    fecha_programada  DATETIME2     NOT NULL,
    fecha_real        DATETIME2     NULL,
    estado            NVARCHAR(20)  NOT NULL CONSTRAINT DF_Ent_estado DEFAULT ('Pendiente'),
                                                                     -- Pendiente, En ruta, Entregada, Fallida
    fk_id_repartidor  INT           NULL,                              -- empleado
    notas             NVARCHAR(MAX) NULL,
    CONSTRAINT fk_ent_pedido      FOREIGN KEY (fk_id_pedido)     REFERENCES Pedido(id_pedido) ON DELETE CASCADE,
    CONSTRAINT fk_ent_repartidor  FOREIGN KEY (fk_id_repartidor) REFERENCES Empleado(id_empleado)
);
GO
