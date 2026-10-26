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
import com.ecommerce.orders_service.sources.builders.OrderTestDataBuilder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShipmentService - Pruebas unitarias HU-09")
class ShipmentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @InjectMocks
    private ShipmentService shipmentService;

    @Test
    @DisplayName("Debe enviar una orden pagada")
    void shouldShipPaidOrder() {

        Order order = OrderTestDataBuilder.anOrder()
                .withStatus(OrderStatus.PAID)
                .build();

        ShippingRequest request = new ShippingRequest();
        request.setStatus(ShipmentStatus.SHIPPED);
        request.setTrackingNumber("RALLY-001");

        when(orderRepository.findByIdForUpdate(order.getId()))
                .thenReturn(Optional.of(order));

        when(shipmentRepository.findByOrderId(order.getId()))
                .thenReturn(Optional.empty());

        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShippingResponse response = shipmentService.updateShipping(
                order.getId(), request
        );

        assertThat(response.getStatus()).isEqualTo(ShipmentStatus.SHIPPED);
        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(response.getTrackingNumber()).isEqualTo("RALLY-001");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

        verify(shipmentRepository).save(any(Shipment.class));
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("No debe enviar una orden cancelada")
    void shouldRejectCancelledOrder() {

        Order order = OrderTestDataBuilder.anOrder()
                .withStatus(OrderStatus.CANCELLED)
                .build();

        ShippingRequest request = new ShippingRequest();
        request.setStatus(ShipmentStatus.SHIPPED);

        when(orderRepository.findByIdForUpdate(order.getId()))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
                shipmentService.updateShipping(order.getId(), request)
        ).isInstanceOf(InvalidOrderStateException.class);

        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Debe rechazar una orden inexistente")
    void shouldRejectNonexistentOrder() {

        Long orderId = 999L;

        ShippingRequest request = new ShippingRequest();
        request.setStatus(ShipmentStatus.SHIPPED);

        when(orderRepository.findByIdForUpdate(orderId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                shipmentService.updateShipping(orderId, request)
        ).isInstanceOf(OrderNotFoundException.class);

        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Debe marcar como entregado un envío previamente enviado")
    void shouldDeliverShippedOrder() {

        Order order = OrderTestDataBuilder.anOrder()
                .withStatus(OrderStatus.SHIPPED)
                .build();

        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setStatus(ShipmentStatus.SHIPPED);
        shipment.setTrackingNumber("RALLY-001");

        ShippingRequest request = new ShippingRequest();
        request.setStatus(ShipmentStatus.DELIVERED);

        when(orderRepository.findByIdForUpdate(order.getId()))
                .thenReturn(Optional.of(order));

        when(shipmentRepository.findByOrderId(order.getId()))
                .thenReturn(Optional.of(shipment));

        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShippingResponse response = shipmentService.updateShipping(
                order.getId(), request
        );

        assertThat(response.getStatus())
                .isEqualTo(ShipmentStatus.DELIVERED);

        assertThat(response.getOrderStatus())
                .isEqualTo(OrderStatus.DELIVERED);

        assertThat(order.getStatus())
                .isEqualTo(OrderStatus.DELIVERED);

        verify(shipmentRepository).save(shipment);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("No debe permitir regresar de DELIVERED a SHIPPED")
    void shouldRejectBackwardShippingTransition() {

        Order order = OrderTestDataBuilder.anOrder()
                .withStatus(OrderStatus.DELIVERED)
                .build();

        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setStatus(ShipmentStatus.DELIVERED);

        ShippingRequest request = new ShippingRequest();
        request.setStatus(ShipmentStatus.SHIPPED);

        when(orderRepository.findByIdForUpdate(order.getId()))
                .thenReturn(Optional.of(order));

        when(shipmentRepository.findByOrderId(order.getId()))
                .thenReturn(Optional.of(shipment));

        assertThatThrownBy(() ->
                shipmentService.updateShipping(order.getId(), request)
        ).isInstanceOf(InvalidOrderStateException.class);

        assertThat(order.getStatus())
                .isEqualTo(OrderStatus.DELIVERED);

        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("No debe enviar una orden que no está pagada")
    void shouldRejectUnpaidOrder() {

        Order order = OrderTestDataBuilder.anOrder()
                .withStatus(OrderStatus.CREATED)
                .build();

        ShippingRequest request = new ShippingRequest();
        request.setStatus(ShipmentStatus.SHIPPED);

        when(orderRepository.findByIdForUpdate(order.getId()))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
                shipmentService.updateShipping(order.getId(), request)
        ).isInstanceOf(InvalidOrderStateException.class)
         .hasMessage("La orden debe estar pagada para iniciar el envío");

        assertThat(order.getStatus())
                .isEqualTo(OrderStatus.CREATED);

        verify(shipmentRepository, never()).save(any(Shipment.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Debe rechazar solicitudes de envío con datos obligatorios faltantes")
    void shouldRejectMissingShippingData() {

        ShippingRequest validRequest = new ShippingRequest();
        validRequest.setStatus(ShipmentStatus.SHIPPED);

        assertThatThrownBy(() ->
                shipmentService.updateShipping(null, validRequest)
        ).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() ->
                shipmentService.updateShipping(1L, null)
        ).isInstanceOf(IllegalArgumentException.class);

        ShippingRequest requestWithoutStatus = new ShippingRequest();

        assertThatThrownBy(() ->
                shipmentService.updateShipping(1L, requestWithoutStatus)
        ).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(orderRepository, shipmentRepository);
    }
}
