package com.ecommerce.orders_service.client;

import com.ecommerce.orders_service.dto.ProductResponse;
import com.ecommerce.orders_service.exceptions.ProductNotFoundException;
import com.ecommerce.orders_service.exceptions.ProductOutOfStockException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Implementación simulada (Mock) de {@link ProductClient} activa bajo el perfil por defecto ("default").
 * Permite ejecutar y probar el microservicio de forma aislada al descargar el repositorio sin depender de MS-Productos.
 */
@Component
@Profile("default")
public class MockProductClient implements ProductClient {

    @Override
    public ProductResponse getProductById(Long productId) {
        if (productId == null || productId == 999L) {
            throw new ProductNotFoundException("Producto no encontrado con id: " + productId);
        }
        if (productId == 888L) {
            return new ProductResponse(
                    "Producto Sin Stock #" + productId,
                    "Producto simulado con inventario agotado",
                    new BigDecimal("100.00"),
                    0
            );
        }
        return new ProductResponse(
                "Producto Simulado #" + productId,
                "Descripción estática de respaldo (perfil default)",
                new BigDecimal("100.00"),
                20
        );
    }

    @Override
    public void reduceStock(Long productId, Integer quantity) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del producto debe ser valido"
            );
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a cero"
            );
        }

        if (productId == 888L || quantity > 20) {
            throw new ProductOutOfStockException(
                    "Stock insuficiente para el producto " + productId
            );
        }
    }

    @Override
    public void restoreStock(Long productId, Integer quantity) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del producto debe ser válido"
            );
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a cero"
            );
        }
    }
}
