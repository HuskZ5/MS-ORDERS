-- ============================================================
-- Rally Java - Ecommerce
-- Microservicio: Orders
-- Archivo: 04_coupons.sql
-- Descripción:
--   Estructura necesaria para la aplicación y registro
--   de cupones de descuento en el carrito de compras.
-- ============================================================

USE ecommerce_db;


-- ============================================================
-- Tabla: coupons
-- Descripción:
--   Define los cupones de descuento disponibles y sus reglas
--   de vigencia, uso, compra mínima y acumulación.
-- ============================================================
CREATE TABLE IF NOT EXISTS coupons (
                                       id BIGINT NOT NULL AUTO_INCREMENT,

                                       code VARCHAR(50) NOT NULL,
    discount_percentage DECIMAL(5, 2) NOT NULL,

    start_date DATETIME NOT NULL,
    expiration_date DATETIME NOT NULL,

    minimum_purchase DECIMAL(10, 2) NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    global_usage_limit INT NULL,
    per_user_usage_limit INT NULL,

    stackable BOOLEAN NOT NULL DEFAULT FALSE,

    PRIMARY KEY (id),
    UNIQUE (code)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

-- ============================================================
-- Restricciones de integridad: coupons
-- ============================================================

ALTER TABLE coupons
    ADD CONSTRAINT chk_coupons_discount_percentage
        CHECK (discount_percentage > 0 AND discount_percentage <= 100);

ALTER TABLE coupons
    ADD CONSTRAINT chk_coupons_dates
        CHECK (expiration_date > start_date);

ALTER TABLE coupons
    ADD CONSTRAINT chk_coupons_minimum_purchase
        CHECK (minimum_purchase IS NULL OR minimum_purchase >= 0);

ALTER TABLE coupons
    ADD CONSTRAINT chk_coupons_global_usage_limit
        CHECK (global_usage_limit IS NULL OR global_usage_limit > 0);

ALTER TABLE coupons
    ADD CONSTRAINT chk_coupons_per_user_usage_limit
        CHECK (per_user_usage_limit IS NULL OR per_user_usage_limit > 0);


-- ============================================================
-- Tabla: cart_coupons
-- Descripción:
--   Relaciona los cupones actualmente aplicados a un carrito.
--   Un carrito puede tener varios cupones siempre que las
--   reglas de acumulación lo permitan.
-- ============================================================
CREATE TABLE IF NOT EXISTS cart_coupons (
                                            id BIGINT NOT NULL AUTO_INCREMENT,

                                            cart_id BIGINT NOT NULL,
                                            coupon_id BIGINT NOT NULL,

                                            applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                            PRIMARY KEY (id)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

-- ============================================================
-- Restricciones y relaciones: cart_coupons
-- ============================================================

ALTER TABLE cart_coupons
    ADD CONSTRAINT fk_cart_coupons_cart
        FOREIGN KEY (cart_id)
            REFERENCES carts(id)
            ON DELETE CASCADE;

ALTER TABLE cart_coupons
    ADD CONSTRAINT fk_cart_coupons_coupon
        FOREIGN KEY (coupon_id)
            REFERENCES coupons(id)
            ON DELETE CASCADE;

ALTER TABLE cart_coupons
    ADD CONSTRAINT uq_cart_coupons_cart_coupon
        UNIQUE (cart_id, coupon_id);

-- ============================================================
-- Tabla: coupon_usages
-- Descripción:
--   Registra los cupones consumidos efectivamente al completar
--   una orden. Se utiliza para controlar los límites globales
--   y por usuario, además de conservar el historial de uso.
-- ============================================================
CREATE TABLE IF NOT EXISTS coupon_usages (
                                             id BIGINT NOT NULL AUTO_INCREMENT,

                                             coupon_id BIGINT NOT NULL,
                                             order_id BIGINT NOT NULL,
                                             user_id BIGINT NOT NULL,

                                             used_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                             PRIMARY KEY (id)
    )
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

-- ============================================================
-- Restricciones y relaciones: coupon_usages
-- ============================================================

ALTER TABLE coupon_usages
    ADD CONSTRAINT fk_coupon_usages_coupon
        FOREIGN KEY (coupon_id)
            REFERENCES coupons(id)
            ON DELETE RESTRICT;

ALTER TABLE coupon_usages
    ADD CONSTRAINT fk_coupon_usages_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id)
            ON DELETE RESTRICT;

ALTER TABLE coupon_usages
    ADD CONSTRAINT uq_coupon_usages_order_coupon
        UNIQUE (order_id, coupon_id);