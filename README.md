# Inventory Logistics API

API REST para la gestión de inventario de una empresa de logística. El proyecto busca mantener separadas las reglas del negocio, la entrada HTTP y la persistencia mediante arquitectura hexagonal.

El desarrollo se realiza de forma incremental. Actualmente está implementado el endpoint de creación de productos.

## Estado actual

| Método | Endpoint | Estado | Descripción |
| --- | --- | --- | --- |
| `POST` | `/api/v1/products` | Implementado | Crea un producto en el inventario |

Los endpoints de consulta, eliminación, movimientos de inventario, reabastecimiento y pedidos se implementarán en features posteriores.

## Tecnologías

- Java 21
- Spring Boot 4.1.1
- Gradle Wrapper
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Flyway
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
ProductController
     │
     ▼
CreateProductUseCase
     │
     ▼
CreateProductService
     │
     ▼
ProductRepositoryPort
     │
     ▼
ProductRepositoryAdapter
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

Los mappers HTTP no conocen entidades JPA y los mappers de persistencia no conocen DTO HTTP.

### Lombok

Lombok se utiliza únicamente para reducir código repetitivo, principalmente constructores y getters. Las reglas de negocio permanecen escritas de forma explícita.

### Concurrencia

La tabla y la entidad de productos incluyen el campo `version` con `@Version`. Esto deja preparado el control optimista de concurrencia para los futuros movimientos que modifiquen el stock.

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

## PostgreSQL

La aplicación utiliza la siguiente organización:

- Base de datos: `logistock`
- Esquema: `inventory`
- Tabla actual: `inventory.products`

La base de datos `logistock` debe existir antes de iniciar la aplicación. Flyway crea el esquema `inventory`, crea sus tablas y mantiene el historial de migraciones dentro de ese esquema.

### Crear la base de datos

Desde `psql`, con un usuario que tenga permiso para crear bases de datos:

```sql
CREATE DATABASE logistock;
```

No es necesario crear manualmente el esquema `inventory` porque Flyway tiene habilitada la opción `create-schemas`.

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

### Migraciones

La primera migración se encuentra en:

```text
src/main/resources/db/migration/V1__create_products_table.sql
```

Hibernate utiliza `ddl-auto: validate`, por lo que valida el esquema pero no crea ni modifica tablas. La evolución de la base de datos corresponde exclusivamente a Flyway.

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
- Inicio del contexto de Spring.

Durante las pruebas se utiliza una base H2 en memoria con compatibilidad PostgreSQL, base lógica `logistock` y esquema `inventory`.

## Flujo de desarrollo

Cada endpoint se desarrolla en una rama `feature/*` independiente. Antes de realizar commits o push, los cambios deben ser revisados manualmente.

La feature actual se encuentra en:

```text
feature/create-product-endpoint
```
