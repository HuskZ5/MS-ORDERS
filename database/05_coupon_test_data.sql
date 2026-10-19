USE ecommerce_db;

INSERT INTO coupons (
    code,
    discount_percentage,
    start_date,
    expiration_date,
    minimum_purchase,
    active,
    global_usage_limit,
    per_user_usage_limit,
    stackable
) VALUES

-- Cupón válido normal, no acumulable
(
    'PROMO10',
    10.00,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    NULL,
    TRUE,
    NULL,
    NULL,
    FALSE
),

-- Cupones válidos y acumulables
(
    'STACK10',
    10.00,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    NULL,
    TRUE,
    NULL,
    NULL,
    TRUE
),
(
    'STACK20',
    20.00,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    NULL,
    TRUE,
    NULL,
    NULL,
    TRUE
),

-- Requiere una compra mínima de $200
(
    'MIN200',
    15.00,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    200.00,
    TRUE,
    NULL,
    NULL,
    FALSE
),

-- Cupón expirado
(
    'EXPIRED10',
    10.00,
    DATE_SUB(NOW(), INTERVAL 30 DAY),
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    NULL,
    TRUE,
    NULL,
    NULL,
    FALSE
),

-- Cupón desactivado
(
    'INACTIVE10',
    10.00,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    NULL,
    FALSE,
    NULL,
    NULL,
    FALSE
),

-- Cupón con límites de uso
(
    'LIMIT10',
    10.00,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    NULL,
    TRUE,
    2,
    1,
    FALSE
),

-- Cupones temporales para validar que el descuento total
-- nunca supere el subtotal del carrito.
-- Permanecen inactivos por defecto y se habilitan solo durante la prueba.
(
    'TEST_CAP60_A',
    60.00,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_ADD(NOW(), INTERVAL 1 DAY),
    NULL,
    FALSE,
    NULL,
    NULL,
    TRUE
),
(
    'TEST_CAP60_B',
    60.00,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_ADD(NOW(), INTERVAL 1 DAY),
    NULL,
    FALSE,
    NULL,
    NULL,
    TRUE
);