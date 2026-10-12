-- ============================================================
-- Rally Java - Ecommerce
-- Microservicio: Orders
-- Archivo: 01_create_tables.sql
-- Descripción:
--   Creación de las tablas pertenecientes al dominio Orders.
--
-- IMPORTANTE:
--   La base de datos ecommerce_db es compartida físicamente
--   entre los microservicios Users, Products y Orders.
--   Este script únicamente crea las tablas propiedad de Orders.
-- ============================================================

USE ecommerce_db;

-- ============================================================
-- Tabla: carts
-- Descripción:
--   Representa el carrito de compras asociado a un usuario.
--
-- user_id:
--   Referencia lógica al usuario propietario del carrito.
--   No se define FK hacia Users MS porque pertenece a otro
--   microservicio.
-- ============================================================
CREATE TABLE IF NOT EXISTS carts (
                                     id BIGINT NOT NULL AUTO_INCREMENT,
                                     user_id BIGINT NOT NULL,

                                     created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_date DATETIME NOT NULL
                                     DEFAULT CURRENT_TIMESTAMP
                                     ON UPDATE CURRENT_TIMESTAMP,

                                     PRIMARY KEY (id)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- ============================================================
-- Tabla: cart_items
-- Descripción:
--   Productos contenidos dentro de un carrito.
--
-- cart_id:
--   Pertenece al dominio Orders y posteriormente tendrá FK
--   hacia carts.
--
-- product_id:
--   Referencia lógica a Products MS.
--   No se define FK física hacia tablas de Products.
-- ============================================================
CREATE TABLE IF NOT EXISTS cart_items (
                                          id BIGINT NOT NULL AUTO_INCREMENT,
                                          cart_id BIGINT NOT NULL,
                                          product_id BIGINT NOT NULL,

                                          quantity INT NOT NULL,
                                          price DECIMAL(10, 2) NOT NULL,

    PRIMARY KEY (id)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- ============================================================
-- Tabla: orders
-- Descripción:
--   Representa una orden generada por un usuario.
--
-- user_id:
--   Referencia lógica a Users MS.
--   No existe FK física hacia tablas de Users.
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      user_id BIGINT NOT NULL,

                                      total DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- ============================================================
-- Tabla: order_items
-- Descripción:
--   Detalle de los productos incluidos en una orden.
--
-- order_id:
--   Pertenece al dominio Orders y posteriormente tendrá FK
--   hacia orders.
--
-- product_id:
--   Referencia lógica a Products MS.
--   No se define FK física hacia Products.
-- ============================================================
CREATE TABLE IF NOT EXISTS order_items (
                                           id BIGINT NOT NULL AUTO_INCREMENT,
                                           order_id BIGINT NOT NULL,
                                           product_id BIGINT NOT NULL,

                                           quantity INT NOT NULL,
                                           unit_price DECIMAL(10, 2) NOT NULL,

    PRIMARY KEY (id)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;