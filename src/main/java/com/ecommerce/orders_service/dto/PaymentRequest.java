
package com.ecommerce.orders_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PaymentRequest {

    @NotBlank(message = "El identificador de operación es obligatorio")
    @Size(max = 100, message = "El identificador no puede superar 100 caracteres")
    private String operationId;

    public PaymentRequest() {}

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
}
