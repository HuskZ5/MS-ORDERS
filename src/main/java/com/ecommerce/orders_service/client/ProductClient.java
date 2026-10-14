package com.ecommerce.orders_service.client;

import com.ecommerce.orders_service.dto.ProductResponse;
import com.ecommerce.orders_service.exceptions.ProductNotFoundException;
import com.ecommerce.orders_service.dto.ReduceStockRequest;
import com.ecommerce.orders_service.exceptions.ProductOutOfStockException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

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
    }

}
