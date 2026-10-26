package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.dto.ShippingRequest;
import com.ecommerce.orders_service.dto.ShippingResponse;
import com.ecommerce.orders_service.exceptions.InvalidOrderStateException;
import com.ecommerce.orders_service.exceptions.OrderNotFoundException;
import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.model.OrderStatus;
import com.ecommerce.orders_service.model.Shipment;
import com.ecommerce.orders_service.model.ShipmentStatus;
import com.ecommerce.orders_service.repository.OrderRepository;
import com.ecommerce.orders_service.repository.ShipmentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipmentService {

    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;

    public ShipmentService(
            OrderRepository orderRepository,
            ShipmentRepository shipmentRepository) {
        this.orderRepository = orderRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public ShippingResponse updateShipping(
            Long orderId,
            ShippingRequest request) {

        if (orderId == null || request == null
                || request.getStatus() == null) {
            throw new IllegalArgumentException(
                    "El ID de la orden y el estado del envío son obligatorios");
        }

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Orden no encontrada con id: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
                    "No se puede enviar una orden cancelada");
        }

        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElse(null);

        if (shipment == null) {
            if (order.getStatus() != OrderStatus.PAID) {
                throw new InvalidOrderStateException(
                        "La orden debe estar pagada para iniciar el envío");
            }

            shipment = new Shipment();
            shipment.setOrder(order);
            shipment.setStatus(ShipmentStatus.PENDING);
        }

        ShipmentStatus currentStatus = shipment.getStatus();
        ShipmentStatus requestedStatus = request.getStatus();

        boolean validTransition =
                (currentStatus == ShipmentStatus.PENDING
                        && requestedStatus == ShipmentStatus.SHIPPED)
                || (currentStatus == ShipmentStatus.SHIPPED
                        && requestedStatus == ShipmentStatus.DELIVERED);

        if (!validTransition) {
            throw new InvalidOrderStateException(
                    "Transición de envío no permitida: "
                            + currentStatus + " -> " + requestedStatus);
        }

        shipment.setStatus(requestedStatus);

        if (request.getTrackingNumber() != null
                && !request.getTrackingNumber().isBlank()) {
            shipment.setTrackingNumber(
                    request.getTrackingNumber().trim());
        }

        if (requestedStatus == ShipmentStatus.SHIPPED) {
            order.setStatus(OrderStatus.SHIPPED);
        } else if (requestedStatus == ShipmentStatus.DELIVERED) {
            order.setStatus(OrderStatus.DELIVERED);
        }

        Shipment savedShipment = shipmentRepository.save(shipment);
        orderRepository.save(order);

        return new ShippingResponse(
                savedShipment.getId(),
                order.getId(),
                savedShipment.getStatus(),
                order.getStatus(),
                savedShipment.getTrackingNumber(),
                savedShipment.getCreatedAt(),
                savedShipment.getUpdatedAt(),
                "Envío actualizado correctamente"
        );
    }
}
