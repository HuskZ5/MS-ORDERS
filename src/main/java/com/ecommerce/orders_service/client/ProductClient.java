package com.ecommerce.orders_service.client;

import com.ecommerce.orders_service.dto.ProductResponse;
import com.ecommerce.orders_service.dto.ReduceStockRequest;
import com.ecommerce.orders_service.exceptions.ProductNotFoundException;
import com.ecommerce.orders_service.exceptions.ProductOutOfStockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(
            @Value("${products.service.url}") String productsServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(productsServiceUrl)
                .build();
    }

    public ProductResponse getProductById(Long productId) {

        // ===================== MOCK LOCAL (Descomentar para pruebas sin MS-Productos) =====================
        if (productId == null || productId == 999L) {
            throw new ProductNotFoundException("Producto no encontrado con id: " + productId);
        }
        if (productId == 888L) {
            return new ProductResponse(
                    "Producto Sin Stock #" + productId,
                    "Producto de prueba con stock agotado",
                    new BigDecimal("100.00"),
                    0
            );
        }
        return new ProductResponse(
                "Producto Simulado #" + productId,
                "Descripción simulada localmente para pruebas",
                new BigDecimal("100.00"),
                20
        );
        // ==================================================================================================

        /*
        // ===================== PRODUCCIÓN (Llamada real con RestClient) ===================================
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
        // ==================================================================================================
        */
    }


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

        // ===================== MOCK LOCAL (Descomentar para pruebas sin MS-Productos) =====================
        if (productId == 888L || quantity > 20) {
            throw new ProductOutOfStockException(
                    "Stock insuficiente para el producto " + productId
            );
        }
        System.out.println("[MOCK LOCAL] Stock reducido -> productId=" + productId + ", quantity=" + quantity);
        // ==================================================================================================

        /*
        // ===================== PRODUCCIÓN (Llamada real con RestClient) ===================================
        try {
            restClient.patch()
                    .uri("/api/products/{id}/stock", productId)
                    //MOCK POSTMAN: Descomentar para simular un error 409 Conflict
                    //.header("x-mock-response-name", "Stock Insuficiente")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ReduceStockRequest(quantity))
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException.Conflict ex) {
            throw new ProductOutOfStockException(
                    "Stock insuficiente para el producto " + productId
            );
        }
        // ==================================================================================================
        */
    }


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

        // ===================== MOCK LOCAL (Descomentar para pruebas sin MS-Productos) =====================
        System.out.println("[MOCK LOCAL] Stock restaurado -> productId=" + productId + ", quantity=" + quantity);
        // ==================================================================================================

        /*
        // ===================== PRODUCCIÓN (Llamada real con RestClient) ===================================
        restClient.patch()
                .uri("/api/products/{id}/stock/restore", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReduceStockRequest(quantity))
                .retrieve()
                .toBodilessEntity();
        // ==================================================================================================
        */
    }

}
