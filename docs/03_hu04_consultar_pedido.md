# HU-04 - Consultar pedido

## Objetivo

Permitir que un cliente consulte la información y el estado de un pedido mediante su ID.

## Endpoint

`GET /api/orders/{id}`

## Criterios cubiertos

- Recibir el ID del pedido.
- Devolver los productos asociados al pedido.
- Mostrar las cantidades de cada producto.
- Mostrar el precio unitario.
- Mostrar el total del pedido.
- Mostrar el estado del pedido.
- Manejar correctamente el caso en que el pedido no exista.

## Flujo de la petición

```text
Cliente
   ↓
OrderController
   ↓
OrderService
   ↓
OrderRepository
   ↓
Base de datos
```

La entidad `Order` se transforma en un `OrderResponse` antes de enviarse al cliente.

## Componentes utilizados

- `OrderController`
- `OrderService`
- `OrderRepository`
- `Order`
- `OrderItem`
- `OrderResponse`
- `OrderItemDto`
- `OrderNotFoundException`
- `GlobalExceptionHandler`

## Respuesta exitosa

Ejemplo:

`GET /api/orders/1`

Respuesta esperada:

`200 OK`

```json
{
  "id": 1,
  "userId": 1,
  "total": 330.00,
  "status": "CREATED",
  "createdAt": "2026-10-08T20:51:03",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 2,
      "unitPrice": 60.00
    },
    {
      "id": 2,
      "productId": 2,
      "quantity": 3,
      "unitPrice": 70.00
    }
  ]
}
```

## Pedido inexistente

Ejemplo:

`GET /api/orders/999`

Respuesta esperada:

`404 Not Found`

```json
{
  "error": "Pedido no encontrado con id: 999"
}
```

## Pruebas funcionales

Las pruebas funcionales se realizaron con Postman.

Casos comprobados:

- Pedido existente → `200 OK`.
- Pedido inexistente → `404 Not Found`.

## Pruebas unitarias

Se implementaron pruebas con JUnit, Mockito y MockMvc.

### OrderServiceTest

- `shouldGetOrderByIdWhenOrderExists`
- `shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist`

### OrderControllerTest

- `shouldReturnOrderWhenOrderExists`
- `shouldReturnNotFoundWhenOrderDoesNotExist`

Resultado de ejecución:

```text
Tests run: 4
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Para ejecutar estas pruebas:

```powershell
.\mvnw.cmd "-Dtest=OrderServiceTest,OrderControllerTest" test
```

## Dependencia pendiente: autorización

La HU establece que únicamente el propietario del pedido o un usuario autorizado puede consultarlo.

Esta validación depende del contrato de autenticación con Users/Auth MS.

Se requiere definir:

- mecanismo de autenticación;
- forma de obtener el `userId` autenticado;
- roles o authorities;
- responsabilidad de validación del token;
- definición de qué roles pueden consultar pedidos ajenos.

No se implementó una solución hardcodeada para evitar acoplar Orders a un mecanismo de autenticación todavía no definido.

## Trazabilidad

Rama:

`feature/hu4-get-order`

Pull Request:

`#13 - feat: implementa HU-04 consulta de pedido por ID`
