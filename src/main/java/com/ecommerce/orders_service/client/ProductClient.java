package com.ecommerce.orders_service.client;

import com.ecommerce.orders_service.dto.ProductResponse;

/**
 * Contrato de cliente para la comunicación con el microservicio de catálogo de productos (MS-Productos).
 */
public interface ProductClient {

    /**
     * Consulta el detalle y stock disponible de un producto por su identificador.
     *
     * @param productId Identificador único del producto.
     * @return {@link ProductResponse} con la información del producto.
     */
    ProductResponse getProductById(Long productId);

    /**
     * Descuenta unidades del inventario de un producto al confirmar una orden.
     *
     * @param productId Identificador único del producto.
     * @param quantity  Cantidad de unidades a descontar.
     */
    void reduceStock(Long productId, Integer quantity);

    /**
     * Restaura unidades al inventario de un producto al cancelar una orden.
     *
     * @param productId Identificador único del producto.
     * @param quantity  Cantidad de unidades a restaurar.
     */
    void restoreStock(Long productId, Integer quantity);
}
