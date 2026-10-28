package com.ecommerce.orders_service.client;

import com.ecommerce.orders_service.dto.ProductResponse;
import com.ecommerce.orders_service.dto.ReduceStockRequest;
import com.ecommerce.orders_service.exceptions.ProductNotFoundException;
import com.ecommerce.orders_service.exceptions.ProductOutOfStockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Implementación real de {@link ProductClient} activa bajo el perfil "local".
 * Realiza peticiones HTTP síncronas mediante {@link RestClient} hacia el microservicio MS-Productos.
 */
@Component
@Profile("local")
public class RealProductClient implements ProductClient {

    private final RestClient restClient;

    public RealProductClient(
            @Value("${products.service.url}") String productsServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(productsServiceUrl)
                .build();
    }

    @Override
    public ProductResponse getProductById(Long productId) {
        try {
            return restClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .body(ProductResponse.class);

        } catch (HttpClientErrorException.NotFound ex) {
            throw new ProductNotFoundException(
                    "Producto no encontrado con id: " + productId
            );
        }
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

        try {
            restClient.patch()
                    .uri("/api/products/{id}/stock", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ReduceStockRequest(quantity))
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException.Conflict ex) {
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

        restClient.patch()
                .uri("/api/products/{id}/stock/restore", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReduceStockRequest(quantity))
                .retrieve()
                .toBodilessEntity();
    }
}
