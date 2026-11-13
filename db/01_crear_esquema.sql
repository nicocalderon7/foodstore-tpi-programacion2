-- =====================================================================
-- Food Store - Creacion del esquema (MySQL 8)
-- Solo se crean las tablas categoria y producto: Usuario, Pedido y
-- DetallePedido viven en memoria y NO tienen tablas.
--
-- Ejecutar como root (o un usuario con permiso para crear bases):
--   mysql -u root -p < db/01_crear_esquema.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS foodstore
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE foodstore;

-- ---------------------------------------------------------------------
-- categoria
-- La regla "nombre unico entre las categorias ACTIVAS" se valida desde
-- Java con una consulta SQL (CategoriaDAO.existeNombreActivo). Un
-- UNIQUE(nombre) comun no sirve, porque bloquearia reutilizar el nombre
-- de una categoria dada de baja.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categoria (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(100)  NOT NULL,
    descripcion  VARCHAR(255)  NOT NULL,
    eliminado    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_categoria PRIMARY KEY (id),
    CONSTRAINT chk_categoria_nombre CHECK (TRIM(nombre) <> '')
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- producto
-- categoria_id materializa la agregacion Categoria 1 -> N Producto.
-- En Java NO es un atributo de Producto (la relacion es unidireccional).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS producto (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(100)   NOT NULL,
    precio        DECIMAL(10,2)  NOT NULL,
    descripcion   VARCHAR(255)   NULL,
    stock         INT            NOT NULL DEFAULT 0,
    imagen        VARCHAR(255)   NULL,
    disponible    BOOLEAN        NOT NULL DEFAULT TRUE,
    eliminado     BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    categoria_id  BIGINT         NOT NULL,
    CONSTRAINT pk_producto PRIMARY KEY (id),
    CONSTRAINT chk_producto_nombre CHECK (TRIM(nombre) <> ''),
    CONSTRAINT chk_producto_precio CHECK (precio >= 0),
    CONSTRAINT chk_producto_stock  CHECK (stock >= 0),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id)
        REFERENCES categoria (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB;
