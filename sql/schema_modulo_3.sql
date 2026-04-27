-- =============================================================
-- Repostería Rosato - Módulo 3: Recetas y Producción
-- Dialecto: SQL Server (T-SQL)
-- Asume que la base 'Reposteria' ya existe y que los
-- módulos 1-2 y 4 ya fueron aplicados (Ingrediente debe existir).
-- =============================================================

USE Reposteria;
GO

CREATE TABLE Receta (
    id_receta        INT IDENTITY(1,1) PRIMARY KEY,
    tipo_producto    NVARCHAR(30)  NOT NULL UNIQUE,                  -- 'Torta', 'Bizcocho', 'Cupcakes', etc.
    nombre           NVARCHAR(100) NOT NULL,
    notas            NVARCHAR(MAX) NULL,
    activa           BIT           NOT NULL CONSTRAINT DF_Receta_act  DEFAULT (1),
    fecha_registro   DATETIME2     NOT NULL CONSTRAINT DF_Receta_fech DEFAULT (SYSDATETIME())
);
GO

CREATE TABLE RecetaIngrediente (
    id_receta_ing        INT IDENTITY(1,1) PRIMARY KEY,
    fk_id_receta         INT           NOT NULL,
    fk_id_ingrediente    INT           NOT NULL,
    cantidad_por_libra   DECIMAL(12,4) NOT NULL,
    CONSTRAINT fk_recing_receta      FOREIGN KEY (fk_id_receta)     REFERENCES Receta(id_receta) ON DELETE CASCADE,
    CONSTRAINT fk_recing_ingrediente FOREIGN KEY (fk_id_ingrediente) REFERENCES Ingrediente(id_ingrediente),
    CONSTRAINT uq_receta_ingrediente UNIQUE (fk_id_receta, fk_id_ingrediente)
);
GO
