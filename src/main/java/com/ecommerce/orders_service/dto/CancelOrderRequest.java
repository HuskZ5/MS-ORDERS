
package com.ecommerce.orders_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la solicitud de cancelación de un pedido.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderRequest {

    @Schema(
            description = "Identificador de la orden que se desea cancelar",
            example = "22"
    )
    private Long orderId;

    @Schema(
            description = "Identificador interno del usuario obtenido del JWT",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long userId;
}

