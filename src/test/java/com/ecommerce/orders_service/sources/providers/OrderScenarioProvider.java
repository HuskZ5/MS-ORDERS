package com.ecommerce.orders_service.sources.providers;

import com.ecommerce.orders_service.exceptions.CartEmptyException;
import com.ecommerce.orders_service.exceptions.CartNotFoundException;
import com.ecommerce.orders_service.exceptions.InvalidOrderStateException;
import com.ecommerce.orders_service.exceptions.OrderNotFoundException;
import com.ecommerce.orders_service.exceptions.ProductOutOfStockException;
import com.ecommerce.orders_service.exceptions.UnauthorizedOrderAccessException;
import com.ecommerce.orders_service.model.OrderStatus;
import com.ecommerce.orders_service.sources.scenarios.order.CancelOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.ControllerCancelOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.ControllerCreateOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.ControllerGetAllOrdersScenario;
import com.ecommerce.orders_service.sources.scenarios.order.ControllerGetOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.ControllerGetTodayOrdersScenario;
import com.ecommerce.orders_service.sources.scenarios.order.CreateOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.GetAllOrdersScenario;
import com.ecommerce.orders_service.sources.scenarios.order.GetOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.GetTodayOrdersScenario;

import java.util.stream.Stream;

/**
 * Proveedor centralizado de escenarios de prueba para el dominio de Pedidos (OrderController y OrderService).
 */
public final class OrderScenarioProvider {

    private OrderScenarioProvider() {
    }

    // =========================================================================
    // ESCENARIOS DE CONTROLADOR (OrderControllerTest)
    // =========================================================================

    public static Stream<ControllerGetAllOrdersScenario> provideControllerGetAllOrdersScenarios() {
        return Stream.of(
                new ControllerGetAllOrdersScenario(
                        "Consulta exitosa de todas las ordenes retorna 200 OK",
                        1L, 1L, "330.00", "CREATED", 1L, 101L, 2, "165.00", 200, null, null
                ),
                new ControllerGetAllOrdersScenario(
                        "Consulta cuando no existen ordenes registradas retorna 404 Not Found",
                        0L, 0L, "0.00", "CREATED", 0L, 0L, 0, "0.00", 404,
                        OrderNotFoundException.class, "No hay ordenes disponibles"
                )
        );
    }

    public static Stream<ControllerGetTodayOrdersScenario> provideControllerGetTodayOrdersScenarios() {
        return Stream.of(
                new ControllerGetTodayOrdersScenario(
                        "Consulta exitosa de las ordenes del dia retorna 200 OK",
                        1L, 1L, "330.00", "CREATED", 1L, 101L, 2, "165.00", 200, null, null
                ),
                new ControllerGetTodayOrdersScenario(
                        "Consulta cuando no existen ordenes registradas hoy retorna 404 Not Found",
                        0L, 0L, "0.00", "CREATED", 0L, 0L, 0, "0.00", 404,
                        OrderNotFoundException.class, "No hay órdenes registradas el día de hoy"
                )
        );
    }

    public static Stream<ControllerGetOrderScenario> provideControllerGetOrderScenarios() {
        return Stream.of(
                new ControllerGetOrderScenario(
                        "Consulta exitosa de pedido existente retorna 200 OK",
                        1L, 1L, "330.00", "CREATED", 1L, 101L, 2, "165.00", 200, null, null
                ),
                new ControllerGetOrderScenario(
                        "Consulta de pedido inexistente retorna 404 Not Found",
                        999L, 1L, null, null, null, 0L, 0, "0.00", 404,
                        OrderNotFoundException.class, "Pedido no encontrado con id: 999"
                )
        );
    }

    public static Stream<ControllerCreateOrderScenario> provideControllerCreateOrderScenarios() {
        return Stream.of(
                new ControllerCreateOrderScenario(
                        "Creacion exitosa de orden desde carrito retorna 201 Created",
                        1L, 5L, "300.00", "CREATED", 1L, 101L, 3, "100.00", 201, null, null
                ),
                new ControllerCreateOrderScenario(
                        "Creacion falla cuando carrito no existe y retorna 404 Not Found",
                        null, 99L, null, null, 0L, 0L, 0, "0.00", 404,
                        CartNotFoundException.class, "Carrito no encontrado para el usuario id: 99"
                ),
                new ControllerCreateOrderScenario(
                        "Creacion falla cuando carrito esta vacio y retorna 400 Bad Request",
                        null, 5L, null, null, 0L, 0L, 0, "0.00", 400,
                        CartEmptyException.class, "El carrito del usuario 5 está vacío"
                ),
                new ControllerCreateOrderScenario(
                        "Creacion falla cuando no hay stock suficiente y retorna 409 Conflict",
                        null, 5L, null, null, 0L, 0L, 0, "0.00", 409,
                        ProductOutOfStockException.class, "Stock insuficiente para el producto 101"
                )
        );
    }

    public static Stream<ControllerCancelOrderScenario> provideControllerCancelOrderScenarios() {
        return Stream.of(
                new ControllerCancelOrderScenario(
                        "Cancelacion exitosa en /cancel retorna 200 OK con reembolso",
                        null, 1L, 1L, OrderStatus.CANCELLED, "200.00", 101L, 2, 200, null, null
                ),
                new ControllerCancelOrderScenario(
                        "Cancelacion exitosa en /{id}/cancel sin orderId en JSON asigna pathId",
                        1L, null, 1L, OrderStatus.CANCELLED, "200.00", 101L, 2, 200, null, null
                ),
                new ControllerCancelOrderScenario(
                        "Cancelacion exitosa en /{id}/cancel con orderId en JSON conserva orderId",
                        1L, 1L, 1L, OrderStatus.CANCELLED, "200.00", 101L, 2, 200, null, null
                ),
                new ControllerCancelOrderScenario(
                        "Cancelacion sin userId retorna 401 Unauthorized",
                        null, 1L, null, null, "0", 0L, 0, 401, null, "Usuario no autenticado"
                ),
                new ControllerCancelOrderScenario(
                        "Cancelacion por usuario no propietario retorna 403 Forbidden",
                        null, 1L, 2L, null, "0", 0L, 0, 403,
                        UnauthorizedOrderAccessException.class,
                        "El usuario con id 2 no tiene autorización para cancelar este pedido"
                ),
                new ControllerCancelOrderScenario(
                        "Cancelacion de pedido en estado invalido retorna 409 Conflict",
                        null, 1L, 1L, null, "0", 0L, 0, 409,
                        InvalidOrderStateException.class,
                        "El pedido con id 1 ya se encuentra cancelado"
                ),
                new ControllerCancelOrderScenario(
                        "Cancelacion de pedido inexistente retorna 404 Not Found",
                        null, 999L, 1L, null, "0", 0L, 0, 404,
                        OrderNotFoundException.class,
                        "Pedido no encontrado con id: 999"
                )
        );
    }

    // =========================================================================
    // ESCENARIOS DE SERVICIO (OrderServiceTest)
    // =========================================================================

    public static Stream<GetAllOrdersScenario> provideGetAllOrdersScenarios() {
        return Stream.of(
                new GetAllOrdersScenario(
                        "Consulta exitosa de todas las ordenes cuando existen registros",
                        true, 1L, 1L, "330.00", OrderStatus.CREATED, 101L, 2, "165.00", 1, null, null
                ),
                new GetAllOrdersScenario(
                        "Falla al consultar todas las ordenes cuando la base de datos esta vacia",
                        false, 0L, 0L, "0.00", OrderStatus.CREATED, 0L, 0, "0.00", 0,
                        OrderNotFoundException.class, "No hay ordenes disponibles"
                )
        );
    }

    public static Stream<GetTodayOrdersScenario> provideGetTodayOrdersScenarios() {
        return Stream.of(
                new GetTodayOrdersScenario(
                        "Consulta exitosa de ordenes del dia cuando existen registros hoy",
                        true, 1L, 1L, "330.00", OrderStatus.CREATED, 101L, 2, "165.00", 1, null, null
                ),
                new GetTodayOrdersScenario(
                        "Falla al consultar ordenes del dia cuando no hay registros hoy",
                        false, 0L, 0L, "0.00", OrderStatus.CREATED, 0L, 0, "0.00", 0,
                        OrderNotFoundException.class, "No hay órdenes registradas el día de hoy"
                )
        );
    }

    public static Stream<GetOrderScenario> provideGetOrderScenarios() {
        return Stream.of(
                new GetOrderScenario(
                        "Consulta exitosa de pedido existente con items",
                        1L, true, 1L, "330.00", OrderStatus.CREATED, 101L, 2, "165.00", null, null
                ),
                new GetOrderScenario(
                        "Falla al consultar pedido inexistente en base de datos",
                        999L, false, 0L, "0.00", OrderStatus.CREATED, 0L, 0, "0.00",
                        OrderNotFoundException.class, "Pedido no encontrado con id: 999"
                )
        );
    }

    public static Stream<CreateOrderScenario> provideCreateOrderScenarios() {
        return Stream.of(
                new CreateOrderScenario(
                        "Creacion exitosa de orden desde carrito reduce stock y calcula total",
                        5L, true, false, 101L, 3, "100.00", "300.00", OrderStatus.CREATED, null, null, null
                ),
                new CreateOrderScenario(
                        "Falla creacion de orden cuando stock es insuficiente",
                        5L, true, false, 101L, 5, "100.00", "0.00", null,
                        ProductOutOfStockException.class,
                        ProductOutOfStockException.class,
                        "Stock insuficiente para el producto 101"
                ),
                new CreateOrderScenario(
                        "Falla creacion de orden cuando carrito no existe",
                        99L, false, true, 0L, 0, "0.00", "0.00", null,
                        null, CartNotFoundException.class, "Carrito no encontrado para el usuario id: 99"
                ),
                new CreateOrderScenario(
                        "Falla creacion de orden cuando carrito esta vacio",
                        5L, true, true, 0L, 0, "0.00", "0.00", null,
                        null, CartEmptyException.class, "El carrito del usuario 5 está vacío"
                )
        );
    }

    public static Stream<CancelOrderScenario> provideCancelOrderScenarios() {
        return Stream.of(
                new CancelOrderScenario(
                        "Cancelacion exitosa de pedido en estado CREATED calcula reembolso",
                        1L, true, OrderStatus.CREATED, 10L, 10L, "250.00", 101L, 2, "125.00",
                        true, OrderStatus.CANCELLED, null, null
                ),
                new CancelOrderScenario(
                        "Cancelacion exitosa de pedido en estado CONFIRMED calcula reembolso",
                        2L, true, OrderStatus.CONFIRMED, 10L, 10L, "150.00", 102L, 1, "150.00",
                        true, OrderStatus.CANCELLED, null, null
                ),
                new CancelOrderScenario(
                        "Falla al intentar cancelar un pedido inexistente",
                        999L, false, OrderStatus.CREATED, 10L, 10L, "0.00", 0L, 0, "0.00",
                        false, null, OrderNotFoundException.class, "Pedido no encontrado con id: 999"
                ),
                new CancelOrderScenario(
                        "Falla al intentar cancelar con orden ya CANCELLED",
                        1L, true, OrderStatus.CANCELLED, 1L, 1L, "100.00", 101L, 1, "100.00",
                        false, null, InvalidOrderStateException.class, "El pedido con id 1 ya se encuentra cancelado"
                ),
                new CancelOrderScenario(
                        "Falla al intentar cancelar con orden en estado SHIPPED",
                        1L, true, OrderStatus.SHIPPED, 1L, 1L, "100.00", 101L, 1, "100.00",
                        false, null, InvalidOrderStateException.class,
                        "No es posible cancelar el pedido con id 1 porque se encuentra en estado: SHIPPED"
                ),
                new CancelOrderScenario(
                        "Falla al intentar cancelar con orden en estado DELIVERED",
                        1L, true, OrderStatus.DELIVERED, 1L, 1L, "100.00", 101L, 1, "100.00",
                        false, null, InvalidOrderStateException.class,
                        "No es posible cancelar el pedido con id 1 porque se encuentra en estado: DELIVERED"
                ),
                new CancelOrderScenario(
                        "Falla al intentar cancelar por usuario no propietario",
                        1L, true, OrderStatus.CREATED, 1L, 2L, "100.00", 101L, 1, "100.00",
                        false, null, UnauthorizedOrderAccessException.class,
                        "El usuario con id 2 no tiene autorización para cancelar este pedido"
                )
        );
    }
}
