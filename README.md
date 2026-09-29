# Inventory Logistics API

API REST para la gestión de inventario de una empresa de logística. El proyecto busca mantener separadas las reglas del negocio, la entrada HTTP y la persistencia mediante arquitectura hexagonal.

El desarrollo se realiza de forma incremental. Actualmente se pueden administrar productos, registrar entradas y salidas de inventario, consultar la trazabilidad global o por producto y crear pedidos.

## Estado actual

| Método | Endpoint | Estado | Descripción |
| --- | --- | --- | --- |
| `POST` | `/api/v1/products` | Implementado | Crea un producto en el inventario |
| `GET` | `/api/v1/products` | Implementado | Lista productos con paginación y filtros |
| `GET` | `/api/v1/products/{id}` | Implementado | Consulta un producto por identificador |
| `GET` | `/api/v1/products/restock` | Implementado | Lista productos con stock menor que cinco |
| `DELETE` | `/api/v1/products/{id}` | Implementado | Elimina un producto únicamente cuando su stock es cero |
| `POST` | `/api/v1/inventory/entries` | Implementado | Registra una entrada e incrementa el stock |
| `POST` | `/api/v1/inventory/exits` | Implementado | Registra una salida y disminuye el stock |
| `GET` | `/api/v1/inventory/movements` | Implementado | Lista todos los movimientos de inventario |
| `GET` | `/api/v1/inventory/movements/product/{productId}` | Implementado | Lista los movimientos de un producto |
| `POST` | `/api/v1/orders` | Implementado | Crea un pedido en estado `CREATED` |
| `GET` | `/api/v1/orders/{id}` | Implementado | Consulta un pedido con todos sus ítems |

El listado, la confirmación y la cancelación de pedidos se implementarán en features posteriores.

## Tecnologías

- Java 21
- Spring Boot 4.1.1
- Gradle Wrapper
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Jakarta Validation
- MapStruct
- Lombok
- Springdoc OpenAPI
- JUnit 5
- Mockito
- H2 en modo PostgreSQL para las pruebas actuales

El proyecto utiliza Gradle. No requiere Maven ni contiene un `pom.xml`.

## Arquitectura

La aplicación sigue una arquitectura hexagonal:

```text
HTTP Request
     │
     ▼
HTTP Controller
     │
     ▼
Input port
     │
     ▼
Use case
     │
     ▼
Output port
     │
     ▼
JPA adapter
     │
     ▼
Spring Data JPA ─────► PostgreSQL
```

### Capas

- `domain/model`: modelos y reglas del negocio.
- `domain/enums`: enumeraciones del dominio.
- `domain/exception`: excepciones conocidas del negocio.
- `domain/port/in`: operaciones que la aplicación expone.
- `domain/port/out`: contratos requeridos para acceder a infraestructura.
- `domain/usecase`: implementación de los casos de uso.
- `infra/adapters/driving/http`: controllers, DTO y mappers HTTP.
- `infra/adapters/driven/jpa`: entidades, repositorios, adapters y mappers de persistencia.
- `configuration/beans`: creación explícita de dependencias mediante `@Bean`.
- `configuration/exceptionhandler`: manejo consistente de errores HTTP.
- `configuration/openapi`: metadatos generales de OpenAPI.

El dominio no depende de Spring ni de JPA. Los DTO HTTP y las entidades JPA son modelos separados.

## Decisiones de implementación

### Inyección de dependencias

Los mappers, adapters, casos de uso, manejadores y controllers se construyen explícitamente mediante métodos `@Bean`. No se utilizan `@Component`, `@Service`, `@Repository` ni `@Autowired` en las clases de la aplicación.

`@RestController` y `@RestControllerAdvice` se conservan como metadatos requeridos por Spring MVC, pero las clases que los utilizan quedan fuera del escaneo automático y son registradas mediante `@Bean`.

### Mappers

MapStruct genera en compilación los mappers entre:

- `CreateProductRequest` y `CreateProductCommand`.
- `Product` y `ProductResponse`.
- `Product` y `ProductEntity`.
- `RegisterInventoryEntryRequest` y `RegisterInventoryEntryCommand`.
- `InventoryMovement` y `InventoryMovementResponse`.
- `InventoryMovement` y `InventoryMovementEntity`.
- `CreateOrderRequest` y `CreateOrderCommand`.
- `Order` y `OrderResponse`.
- `Order`, `OrderItem`, `OrderEntity` y `OrderItemEntity`.

Los mappers HTTP no conocen entidades JPA y los mappers de persistencia no conocen DTO HTTP.

### Lombok

Lombok se utiliza únicamente para reducir código repetitivo, principalmente constructores y getters. Las reglas de negocio permanecen escritas de forma explícita.

### Concurrencia

La tabla y la entidad de productos incluyen el campo `version` con `@Version`. Las entradas y salidas usan este control optimista para detectar modificaciones concurrentes. Si dos operaciones intentan cambiar simultáneamente la misma versión del producto, una de ellas devuelve `409 Conflict` con el código `CONCURRENT_INVENTORY_UPDATE` y debe reintentarse.

### Transacciones

La consulta, validación de stock y eliminación del producto se ejecutan dentro de una misma transacción. Las entradas y salidas también actualizan el producto y registran el movimiento dentro de una sola transacción, evitando que uno de los dos cambios quede persistido sin el otro. La creación de un pedido valida todos sus productos y guarda la cabecera y sus ítems atómicamente. El dominio continúa desacoplado de Spring: las transacciones se aplican desde la configuración mediante `TransactionTemplate` y los casos de uso se registran con `@Bean`.

## Modelo de producto

Un producto contiene:

| Campo | Tipo | Regla |
| --- | --- | --- |
| `id` | `Long` | Generado por la base de datos |
| `name` | `String` | Obligatorio, no vacío, máximo 120 caracteres |
| `description` | `String` | Opcional, máximo 500 caracteres |
| `category` | `ProductCategory` | Obligatoria |
| `stock` | `int` | Igual o mayor que cero |
| `price` | `BigDecimal` | Mayor que cero |
| `createdAt` | `Instant` | Generado en UTC al crear el producto |
| `updatedAt` | `Instant` | Generado en UTC al crear el producto |
| `version` | `Long` | Utilizado para optimistic locking |

Categorías disponibles:

- `ELECTRONICS`
- `FOOD`
- `CLOTHING`
- `HOME`
- `OTHER`

Las restricciones importantes se validan tanto en la frontera HTTP como dentro del dominio. De esta manera, las reglas no dependen exclusivamente de `@Valid`.

## Modelo de movimiento de inventario

Cada movimiento contiene:

| Campo | Tipo | Regla |
| --- | --- | --- |
| `id` | `Long` | Generado por la base de datos |
| `productId` | `Long` | Producto asociado al movimiento |
| `type` | `InventoryMovementType` | `ENTRY` o `EXIT` |
| `quantity` | `int` | Debe ser mayor que cero |
| `createdAt` | `Instant` | Fecha UTC generada por la aplicación |
| `observation` | `String` | Opcional, máximo 500 caracteres |

Cada entrada incrementa el stock y cada salida lo disminuye. Ambos movimientos conservan un registro separado para garantizar la trazabilidad.

## Modelo de pedido

Un pedido contiene:

| Campo | Tipo | Regla |
| --- | --- | --- |
| `id` | `Long` | Generado por la base de datos |
| `createdAt` | `Instant` | Fecha UTC generada al crear el pedido |
| `status` | `OrderStatus` | Se inicializa en `CREATED` |
| `items` | `List<OrderItem>` | Debe contener al menos un ítem |

Cada ítem contiene un `productId` válido y una `quantity` mayor que cero. Un producto solo puede aparecer una vez dentro del mismo pedido.

Al crear el pedido se valida que cada producto exista y tenga stock suficiente en ese momento. La creación no reserva ni disminuye existencias; el stock se volverá a validar y se descontará cuando se implemente la confirmación del pedido.

## PostgreSQL

La aplicación utiliza la siguiente organización:

- Base de datos: `logistock`
- Esquema: `inventory`
- Tablas actuales: `inventory.products`, `inventory.inventory_movements`, `inventory.orders` e `inventory.order_items`

La base de datos `logistock` debe existir antes de iniciar la aplicación. Hibernate crea el esquema `inventory` y crea o actualiza sus tablas a partir de las entidades JPA.

### Crear la base de datos

Desde `psql`, con un usuario que tenga permiso para crear bases de datos:

```sql
CREATE DATABASE logistock;
```

No es necesario crear manualmente el esquema `inventory`. La propiedad `hibernate.hbm2ddl.create_namespaces` permite que Hibernate cree el esquema y `ddl-auto: update` crea o actualiza las tablas.

### Variables de entorno

| Variable | Valor predeterminado | Descripción |
| --- | --- | --- |
| `DB_HOST` | `localhost` | Servidor PostgreSQL |
| `DB_PORT` | `5432` | Puerto PostgreSQL |
| `DB_NAME` | `logistock` | Nombre de la base de datos |
| `DB_USERNAME` | `postgres` | Usuario de conexión |
| `DB_PASSWORD` | `postgres` | Contraseña de conexión local |

Ejemplo:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=logistock
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

Las credenciales predeterminadas son únicamente una ayuda para desarrollo local. En otros entornos deben proporcionarse mediante variables de entorno.

### Creación del esquema con JPA

La configuración utilizada es equivalente a:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        default_schema: inventory
        hbm2ddl:
          create_namespaces: true
```

Las entidades están asociadas explícitamente con sus tablas dentro del esquema `inventory`. La columna `product_id` mantiene la relación de movimientos e ítems con el producto, y `order_id` relaciona cada ítem con su pedido. En un ambiente productivo sería recomendable reemplazar `ddl-auto: update` por migraciones versionadas, pero en el alcance actual la estructura se administra exclusivamente mediante JPA/Hibernate.

## Ejecutar localmente

### Requisitos

- JDK 21
- PostgreSQL en ejecución
- Base de datos `logistock` creada

No es necesario instalar Gradle porque el repositorio incluye Gradle Wrapper.

### Iniciar la aplicación

En macOS o Linux:

```bash
./gradlew bootRun
```

En Windows:

```powershell
gradlew.bat bootRun
```

Por defecto, la API queda disponible en `http://localhost:8080`.

## Crear un producto

### Request

```http
POST /api/v1/products
Content-Type: application/json
```

```json
{
  "name": "Barcode scanner",
  "description": "Handheld scanner used in warehouse operations",
  "category": "ELECTRONICS",
  "stock": 12,
  "price": 245.90
}
```

Ejemplo con cURL:

```bash
curl --request POST \
  --url http://localhost:8080/api/v1/products \
  --header 'Content-Type: application/json' \
  --data '{
    "name": "Barcode scanner",
    "description": "Handheld scanner used in warehouse operations",
    "category": "ELECTRONICS",
    "stock": 12,
    "price": 245.90
  }'
```

### Respuesta exitosa

La API devuelve `201 Created` y una cabecera `Location` con la URL del producto creado.

```http
HTTP/1.1 201 Created
Location: http://localhost:8080/api/v1/products/1
```

```json
{
  "id": 1,
  "name": "Barcode scanner",
  "description": "Handheld scanner used in warehouse operations",
  "category": "ELECTRONICS",
  "stock": 12,
  "price": 245.90,
  "createdAt": "2026-09-28T20:00:00Z",
  "updatedAt": "2026-09-28T20:00:00Z"
}
```

## Manejo de errores

Las validaciones HTTP y las excepciones conocidas producen una estructura consistente.

Ejemplo de respuesta `400 Bad Request`:

```json
{
  "timestamp": "2026-09-28T20:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "The request contains invalid fields",
  "path": "/api/v1/products",
  "details": {
    "name": "must not be blank",
    "stock": "must be greater than or equal to 0",
    "price": "must be greater than 0.0"
  }
}
```

## Consultar un producto por ID

### Request

```http
GET /api/v1/products/1
```

Ejemplo con cURL:

```bash
curl --request GET \
  --url http://localhost:8080/api/v1/products/1
```

### Respuesta exitosa

La API devuelve `200 OK` cuando encuentra el producto.

```json
{
  "id": 1,
  "name": "Barcode scanner",
  "description": "Handheld scanner used in warehouse operations",
  "category": "ELECTRONICS",
  "stock": 12,
  "price": 245.90,
  "createdAt": "2026-09-28T20:00:00Z",
  "updatedAt": "2026-09-28T20:00:00Z"
}
```

Si el producto no existe, la API devuelve `404 Not Found`:

```json
{
  "timestamp": "2026-09-28T20:00:00Z",
  "status": 404,
  "error": "Not Found",
  "code": "PRODUCT_NOT_FOUND",
  "message": "Product not found with id 10",
  "path": "/api/v1/products/10",
  "details": {}
}
```

Los identificadores iguales o menores que cero producen `400 Bad Request` con el código `INVALID_PRODUCT_ID`.

## Listar productos

```http
GET /api/v1/products
```

El endpoint acepta los siguientes parámetros opcionales:

| Parámetro | Predeterminado | Descripción |
| --- | --- | --- |
| `category` | Sin filtro | Categoría del producto |
| `minStock` | Sin filtro | Stock mínimo inclusivo |
| `maxStock` | Sin filtro | Stock máximo inclusivo |
| `page` | `0` | Número de página, comenzando en cero |
| `size` | `10` | Elementos por página, entre 1 y 100 |

Los filtros pueden combinarse:

```http
GET /api/v1/products?category=ELECTRONICS&minStock=5&maxStock=50&page=0&size=10
```

Ejemplo con cURL:

```bash
curl --request GET \
  --url 'http://localhost:8080/api/v1/products?category=ELECTRONICS&minStock=5&maxStock=50&page=0&size=10'
```

Respuesta `200 OK`:

```json
{
  "content": [
    {
      "id": 1,
      "name": "Barcode scanner",
      "description": "Handheld scanner used in warehouse operations",
      "category": "ELECTRONICS",
      "stock": 12,
      "price": 245.90,
      "createdAt": "2026-09-28T20:00:00Z",
      "updatedAt": "2026-09-28T20:00:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

Reglas de validación:

- `minStock` y `maxStock` no pueden ser negativos.
- Cuando ambos están presentes, `minStock` debe ser menor o igual a `maxStock`.
- `page` debe ser igual o mayor que cero.
- `size` debe estar entre 1 y 100.

Un rango de stock inválido devuelve `400 Bad Request` con el código `INVALID_STOCK_RANGE`. Una paginación inválida devuelve `400 Bad Request` con el código `INVALID_PAGINATION`.

## Eliminar un producto

```http
DELETE /api/v1/products/{id}
```

Solo se puede eliminar un producto cuyo stock actual sea igual a cero.

Ejemplo:

```bash
curl --request DELETE \
  --url http://localhost:8080/api/v1/products/1
```

Respuestas:

| Estado | Descripción |
| --- | --- |
| `204 No Content` | El producto fue eliminado |
| `400 Bad Request` | El identificador es inválido |
| `404 Not Found` | El producto no existe |
| `409 Conflict` | El producto todavía tiene stock |

Ejemplo de conflicto:

```json
{
  "timestamp": "2026-09-28T20:00:00Z",
  "status": 409,
  "error": "Conflict",
  "code": "PRODUCT_HAS_STOCK",
  "message": "Product 1 cannot be deleted because it has 5 units in stock",
  "path": "/api/v1/products/1",
  "details": {}
}
```

## Productos por reabastecer

```http
GET /api/v1/products/restock?page=0&size=10
```

Un producto requiere reabastecimiento cuando su stock es menor que cinco unidades. El umbral es una regla fija del dominio y no puede modificarse mediante parámetros HTTP.

El endpoint acepta:

| Parámetro | Predeterminado | Descripción |
| --- | --- | --- |
| `page` | `0` | Número de página, comenzando en cero |
| `size` | `10` | Elementos por página, entre 1 y 100 |

Ejemplo:

```bash
curl --request GET \
  --url 'http://localhost:8080/api/v1/products/restock?page=0&size=10'
```

Respuesta `200 OK`:

```json
{
  "content": [
    {
      "id": 1,
      "name": "Low stock scanner",
      "description": "Warehouse device",
      "category": "ELECTRONICS",
      "stock": 4,
      "price": 245.90,
      "createdAt": "2026-09-28T20:00:00Z",
      "updatedAt": "2026-09-28T20:00:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

Una paginación inválida devuelve `400 Bad Request` con el código `INVALID_PAGINATION`.

## Listar movimientos por producto

```http
GET /api/v1/inventory/movements/product/{productId}?page=0&size=10
```

El endpoint devuelve únicamente los movimientos relacionados con el producto solicitado, desde el más reciente hasta el más antiguo.

Ejemplo:

```bash
curl --request GET \
  --url 'http://localhost:8080/api/v1/inventory/movements/product/1?page=0&size=10'
```

Respuesta `200 OK`:

```json
{
  "content": [
    {
      "id": 2,
      "productId": 1,
      "type": "EXIT",
      "quantity": 5,
      "createdAt": "2026-09-28T21:00:00Z",
      "observation": "Customer shipment"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

Comportamiento:

- Un producto existente sin movimientos devuelve `200 OK` con `content` vacío.
- Un identificador igual o menor que cero devuelve `400 Bad Request` con `INVALID_PRODUCT_ID`.
- Un producto inexistente devuelve `404 Not Found` con `PRODUCT_NOT_FOUND`.
- Una paginación inválida devuelve `400 Bad Request` con `INVALID_PAGINATION`.

## Registrar una entrada de inventario

```http
POST /api/v1/inventory/entries
Content-Type: application/json
```

Request:

```json
{
  "productId": 1,
  "quantity": 20,
  "observation": "Supplier delivery"
}
```

Ejemplo con cURL:

```bash
curl --request POST \
  --url http://localhost:8080/api/v1/inventory/entries \
  --header 'Content-Type: application/json' \
  --data '{
    "productId": 1,
    "quantity": 20,
    "observation": "Supplier delivery"
  }'
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "productId": 1,
  "type": "ENTRY",
  "quantity": 20,
  "createdAt": "2026-09-28T20:00:00Z",
  "observation": "Supplier delivery"
}
```

Reglas:

- El producto debe existir.
- La cantidad debe ser mayor que cero.
- La entrada incrementa el stock del producto.
- La actualización del stock y el movimiento se guardan en una misma transacción.
- La observación es opcional y admite máximo 500 caracteres.

Una petición inválida devuelve `400 Bad Request`. Un producto inexistente devuelve `404 Not Found` con el código `PRODUCT_NOT_FOUND`.

## Registrar una salida de inventario

```http
POST /api/v1/inventory/exits
Content-Type: application/json
```

Request:

```json
{
  "productId": 1,
  "quantity": 5,
  "observation": "Customer shipment"
}
```

Ejemplo con cURL:

```bash
curl --request POST \
  --url http://localhost:8080/api/v1/inventory/exits \
  --header 'Content-Type: application/json' \
  --data '{
    "productId": 1,
    "quantity": 5,
    "observation": "Customer shipment"
  }'
```

Respuesta `201 Created`:

```json
{
  "id": 2,
  "productId": 1,
  "type": "EXIT",
  "quantity": 5,
  "createdAt": "2026-09-28T21:00:00Z",
  "observation": "Customer shipment"
}
```

Reglas:

- El producto debe existir.
- La cantidad debe ser mayor que cero.
- La cantidad no puede superar el stock disponible.
- El stock nunca puede quedar negativo.
- La reducción del stock y el movimiento se guardan en una misma transacción.
- La observación es opcional y admite máximo 500 caracteres.

Cuando el stock es insuficiente, la API devuelve `409 Conflict`:

```json
{
  "timestamp": "2026-09-28T21:00:00Z",
  "status": 409,
  "error": "Conflict",
  "code": "INSUFFICIENT_STOCK",
  "message": "Insufficient stock for product 1. Available: 3, requested: 5",
  "path": "/api/v1/inventory/exits",
  "details": {}
}
```

## Listar movimientos de inventario

```http
GET /api/v1/inventory/movements?page=0&size=10
```

El endpoint devuelve movimientos `ENTRY` y `EXIT`, ordenados desde el más reciente hasta el más antiguo. Cuando dos movimientos tienen la misma fecha, el identificador descendente garantiza un orden estable.

Parámetros:

| Parámetro | Predeterminado | Descripción |
| --- | --- | --- |
| `page` | `0` | Número de página, comenzando en cero |
| `size` | `10` | Elementos por página, entre 1 y 100 |

Ejemplo:

```bash
curl --request GET \
  --url 'http://localhost:8080/api/v1/inventory/movements?page=0&size=10'
```

Respuesta `200 OK`:

```json
{
  "content": [
    {
      "id": 2,
      "productId": 1,
      "type": "EXIT",
      "quantity": 5,
      "createdAt": "2026-09-28T21:00:00Z",
      "observation": "Customer shipment"
    },
    {
      "id": 1,
      "productId": 1,
      "type": "ENTRY",
      "quantity": 20,
      "createdAt": "2026-09-28T20:00:00Z",
      "observation": "Supplier delivery"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 2,
  "totalPages": 1,
  "last": true
}
```

Una paginación inválida devuelve `400 Bad Request` con el código `INVALID_PAGINATION`.

## Crear un pedido

```http
POST /api/v1/orders
Content-Type: application/json
```

Request:

```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 3
    },
    {
      "productId": 2,
      "quantity": 2
    }
  ]
}
```

Ejemplo con cURL:

```bash
curl --request POST \
  --url http://localhost:8080/api/v1/orders \
  --header 'Content-Type: application/json' \
  --data '{
    "items": [
      {"productId": 1, "quantity": 3},
      {"productId": 2, "quantity": 2}
    ]
  }'
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "createdAt": "2026-09-28T22:00:00Z",
  "status": "CREATED",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 3
    },
    {
      "id": 2,
      "productId": 2,
      "quantity": 2
    }
  ]
}
```

Reglas:

- El pedido debe contener al menos un ítem.
- Cada producto debe existir y solo puede aparecer una vez.
- Cada cantidad debe ser mayor que cero y no superar el stock disponible.
- El pedido se crea en estado `CREATED`.
- Crear el pedido no reserva ni descuenta stock.
- La cabecera y los ítems se guardan en una única transacción.

Una petición inválida devuelve `400 Bad Request`. Un producto inexistente devuelve `404 Not Found` con `PRODUCT_NOT_FOUND`. Una cantidad superior al stock devuelve `409 Conflict` con `INSUFFICIENT_STOCK`.

## Consultar un pedido por ID

```http
GET /api/v1/orders/{id}
```

Ejemplo con cURL:

```bash
curl --request GET \
  --url http://localhost:8080/api/v1/orders/1
```

Respuesta `200 OK`:

```json
{
  "id": 1,
  "createdAt": "2026-09-28T22:00:00Z",
  "status": "CREATED",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 3
    }
  ]
}
```

El pedido se devuelve con todos sus ítems. Un identificador igual o menor que cero produce `400 Bad Request` con `INVALID_ORDER_ID`. Si el pedido no existe, la respuesta es `404 Not Found` con `ORDER_NOT_FOUND`.

## Swagger y OpenAPI

Con la aplicación en ejecución:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Especificación JSON: `http://localhost:8080/v3/api-docs`
- Especificación YAML: `http://localhost:8080/v3/api-docs.yaml`

## Pruebas

Ejecutar la suite completa:

```bash
./gradlew clean test
```

Las pruebas actuales cubren:

- Creación de un producto válido.
- Normalización de nombre y descripción.
- Rechazo de stock negativo.
- Rechazo de nombre vacío.
- Rechazo de precio no positivo.
- Ejecución del caso de uso y persistencia mediante el puerto de salida.
- Creación de producto desde HTTP.
- Respuesta HTTP ante datos inválidos.
- Publicación del endpoint en OpenAPI.
- Consulta de un producto existente por ID.
- Respuesta `404` para productos inexistentes.
- Rechazo de identificadores inválidos.
- Listado paginado de productos.
- Filtros combinados por categoría y rango de stock.
- Validación de rangos de stock.
- Validación de página y tamaño.
- Eliminación de productos con stock igual a cero.
- Rechazo de eliminación cuando existe stock.
- Respuestas de eliminación para productos inexistentes e identificadores inválidos.
- Identificación de productos con stock menor que cinco.
- Exclusión de productos con stock igual o mayor que cinco.
- Paginación de productos por reabastecer.
- Registro de entradas de inventario.
- Incremento del stock asociado.
- Persistencia de la trazabilidad del movimiento.
- Rechazo de cantidades no positivas y productos inexistentes.
- Registro de salidas de inventario.
- Disminución del stock sin permitir valores negativos.
- Rechazo de salidas superiores al stock disponible.
- Persistencia de movimientos `EXIT`.
- Listado paginado de movimientos de inventario.
- Orden descendente por fecha e identificador.
- Validación de paginación para la trazabilidad.
- Listado paginado de movimientos por producto.
- Aislamiento de movimientos pertenecientes a otros productos.
- Página vacía para productos sin movimientos.
- Validación de producto existente e identificador válido.
- Creación de pedidos con uno o varios productos.
- Persistencia de la cabecera y los ítems del pedido.
- Estado inicial `CREATED` y fecha de creación UTC.
- Validación de pedidos vacíos, productos repetidos e identificadores inválidos.
- Rechazo de productos inexistentes o con stock insuficiente.
- Conservación del stock durante la creación del pedido.
- Publicación de la creación de pedidos en OpenAPI.
- Consulta de un pedido con todos sus ítems.
- Respuesta `404` para pedidos inexistentes.
- Rechazo de identificadores de pedido inválidos.
- Publicación de la consulta de pedidos en OpenAPI.
- Inicio del contexto de Spring.

Durante las pruebas se utiliza una base H2 en memoria con compatibilidad PostgreSQL, base lógica `logistock` y esquema `inventory`.

## Flujo de desarrollo

Cada endpoint se desarrolla en una rama `feature/*` independiente. Antes de realizar commits o push, los cambios deben ser revisados manualmente.

La feature actual se encuentra en:

```text
feature/get-order
```
