-- ============================================================
-- Rally Java - Ecommerce
-- Microservicio: Orders
-- Archivo: 02_constraints.sql
-- Descripción:
--   Restricciones y relaciones del dominio Orders.
-- ============================================================

USE ecommerce_db;

-- ============================================================
-- Foreign Keys internas del microservicio Orders
-- ============================================================

ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id)
            REFERENCES carts(id)
            ON DELETE CASCADE;

ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id)
            ON DELETE CASCADE;


-- ============================================================
-- Restricciones de integridad
-- ============================================================

ALTER TABLE cart_items
    ADD CONSTRAINT chk_cart_items_quantity
        CHECK (quantity > 0);

ALTER TABLE cart_items
    ADD CONSTRAINT chk_cart_items_price
        CHECK (price >= 0);

ALTER TABLE order_items
    ADD CONSTRAINT chk_order_items_quantity
        CHECK (quantity > 0);

ALTER TABLE order_items
    ADD CONSTRAINT chk_order_items_unit_price
        CHECK (unit_price >= 0);

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_total
        CHECK (total >= 0);

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_status
        CHECK (status IN ('CREATED', 'CANCELLED'));


-- ============================================================
-- Restricciones de unicidad
-- ============================================================

-- Un producto solo debe aparecer una vez dentro de un carrito.
-- Si vuelve a agregarse, se actualiza su cantidad.
ALTER TABLE cart_items
    ADD CONSTRAINT uq_cart_items_cart_product
        UNIQUE (cart_id, product_id);

-- Un producto solo debe aparecer una vez dentro de una orden.
ALTER TABLE order_items
    ADD CONSTRAINT uq_order_items_order_product
        UNIQUE (order_id, product_id);