package com.ecommerce.orders_service.client;

import com.ecommerce.orders_service.dto.ProductResponse;
import com.ecommerce.orders_service.exceptions.ProductNotFoundException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
}
