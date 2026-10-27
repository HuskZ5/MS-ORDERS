# Orders Service

## Microservicio de pedidos y carrito del proyecto E-Commerce.

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-6DB33F?style=for-the-badge&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring_Security-Enabled-6DB33F?style=for-the-badge&logo=springsecurity)
![OAuth2](https://img.shields.io/badge/OAuth2-Resource_Server-blue?style=for-the-badge)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-Hibernate-59666C?style=for-the-badge)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger-85EA2D?style=for-the-badge&logo=swagger)
![JUnit](https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5)
![REST](https://img.shields.io/badge/API-REST-blue?style=for-the-badge)
![Microservice](https://img.shields.io/badge/Architecture-Microservice-purple?style=for-the-badge)


<img width="1200" height="630" alt="Order Management - Header" src="https://github.com/user-attachments/assets/5148a398-4062-4a9c-9f24-55fd268cd887" />

## Requisitos

- **Java** 17
- **MySQL 8.4**
- **Git**
- **IntelliJ IDEA o cualquier IDE compatible**

*No es necesario tener Maven instalado globalmente porque el proyecto incluye Maven Wrapper.*

# ✨Funcionalidades
## Carrito
- **Agregar productos**
- **Consultar carrito**
- **Aplicar cupones**

## Órdenes
- **Crear órdenes**
- **Consultar órdenes**
- **Cancelar órdenes**
  
## Pagos
- **Procesar pagos**
- **Validar propiedad de órdenes**

## Envíos
- **Actualizar estado de envío**

## Arquitectura
````
├── client
├── config
├── controllers
├── dto
├── exceptions
├── model
├── repository
└── service

````
## Responsabilidades de los paquetes

| Paquete | Responsabilidad |
| --- | --- |
| `client` | Comunicación externa |
| `config` | Configuración |
| `controllers` | Endpoints REST |
| `dto` | Contratos de API |
| `exceptions` | Manejo de errores |
| `model` | Dominio de negocio |
| `repository` | Persistencia |
| `service` | Lógica de negocio |

# Modelo de Dominio
## Entidades principales
| Entidad | Descripción |
| --- | --- |
| `Cart` | Carrito de compras |
| `CartItem` | Productos del carrito |
| `Coupon` | Cupón de descuento |
| `CouponUsage` | Registro de uso de cupones |
| `Order` | Pedido |
| `OrderItem` | Productos de una orden |
| `Shipment` | Envío |
| `OrderStatus` | Estado de pedido |
| `PaymentStatus` | Estado de pago |
| `ShipmentStatus` | Estado de envío |

# Relación entre entidades
````
Cart
 └── CartItem
       │
       ▼
Order
 └── OrderItem
       │
       ▼
PaymentStatus
       │
       ▼
Shipment
       │
       ▼
ShipmentStatus

Coupon
 └── CouponUsage
````

## 🌐 API REST
### Cart API

| Método | Endpoint |
| --- | --- |
| `POST` | `/api/carts/items` |
| `GET` | `/api/carts/{userId}` |
| `GET` | `/api/carts/me` |
| `POST` | `/api/carts/{userId}/coupons` |

### Orders API
| Método | Endpoint |
| --- | --- |
| `GET` | `/api/orders` |
| `GET` | `/api/orders/today` |
| `GET` | `/api/orders/{id}` |
| `POST` | `/api/orders?userId={id}` |
| `PATCH` | `/api/orders/cancel` |
| `PATCH` | `/api/orders/{id}/cancel` |

### Payments API
| Método | Endpoint |
| --- | --- |
| `POST` | `/api/orders/{id}/payment` |

### Shipment API
| Método | Endpoint |
| --- | --- |
| `PATCH` | `/api/orders/{id}/shipping` |

# 📚 Swagger / OpenAPI
http://localhost:8080/swagger-ui/index.html`
`
# 🧪 Testing
```
test
├── config
├── controllers
├── service
└── sources
    ├── builders
    ├── providers
    └── scenarios
        ├── cart
        ├── coupon
        └── order
```
## Cobertura de pruebas
- **Controllers**
- **Services**
- **Security**
- **Validations**
- **Business Scenarios**
- 
# ✅ Buenas prácticas implementadas
- **Arquitectura por capas**
- **DTO Pattern**
- **Repository Pattern**
- **Dependency Injection**
- **Exception Handling centralizado**
- **JWT Authentication**
- **OAuth2 Resource Server**
- **OpenAPI Documentation**
- **Testing basado en escenarios**
- **Separación de responsabilidades**

## Base de datos

Crear la base:

```sql
CREATE DATABASE orders_db;
```

Se recomienda crear un usuario para la aplicación:

```sql
CREATE USER 'orders_app'@'localhost'
IDENTIFIED BY 'TU_PASSWORD';

GRANT ALL PRIVILEGES ON orders_db.* TO 'orders_app'@'localhost';

FLUSH PRIVILEGES;
```

## Variables de entorno

Antes de ejecutar el proyecto, configurar:

```text
DB_USERNAME
DB_PASSWORD
```
# 🔧 Configuración

**El proyecto utiliza:** 

 ```properties
spring.datasource.url=jdbc:mysql://localhost:3306/orders_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```
**En PowerShell:**

```powershell
$env:DB_USERNAME="orders_app"
$env:DB_PASSWORD="TU_PASSWORD"
```
## ▶️ Ejecutar la aplicación
````
.\mvnw.cmd spring-boot:run
````
## Ejecutar pruebas

**En Windows:**

```powershell
.\mvnw.cmd clean test
```

Si todo está configurado correctamente debe aparecer:

```text
BUILD SUCCESS
```

# 🔒 Consideraciones de Seguridad
- No subir credenciales al repositorio.
- No almacenar secretos en código fuente.
- No subir archivos .env.
- Gestionar credenciales mediante variables de entorno.
