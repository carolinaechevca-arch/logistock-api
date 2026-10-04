# Inventory Logistics API

API REST para la gestión de inventario de una empresa de logística. El proyecto busca mantener separadas las reglas del negocio, la entrada HTTP y la persistencia mediante arquitectura hexagonal.

El desarrollo se realiza de forma incremental. Actualmente se pueden administrar productos, registrar entradas y salidas de inventario, consultar la trazabilidad global o por producto y crear pedidos.

## Integrantes y entregables

| Integrante | Responsabilidad en la práctica |
| --- | --- |
| Sebastian Restrepo Mira |Dockers|
| Mariana González |Api|
| Ferney López Copete |Kubernetes|
| Juan Camilo Duarte Vasco | Validación y evidencias |
| <<Nombre completo>> | Documentación y coordinación |

**Video de implementación:** <<enlace de YouTube>>

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
| `GET` | `/api/v1/orders` | Implementado | Lista pedidos de forma paginada |
| `GET` | `/api/v1/orders/{id}` | Implementado | Consulta un pedido con todos sus ítems |

La confirmación y la cancelación de pedidos se implementarán en features posteriores.

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

## Docker

La API se contenoriza con un `Dockerfile` multi-stage y se ejecuta junto con un contenedor de PostgreSQL conectados por una red de Docker. La imagen se etiqueta `practica2-api:v1`, nombre que también usan los manifiestos de Kubernetes.

### Requisitos

- Docker Desktop en ejecución
- Puerto `8080` libre en la máquina local

No se necesita JDK ni Gradle en la máquina: la compilación ocurre dentro del build de la imagen.

### Puertos

| Servicio | Puerto en el contenedor | Puerto publicado en el host |
| --- | --- | --- |
| API (Spring Boot) | `8080` | `8080` |
| PostgreSQL | `5432` | No se publica; solo es accesible desde la red `practica2-net` |

### Construir la imagen

```bash
docker build -t practica2-api:v1 .
docker images practica2-api
```

![Imagen Docker generada](docs/evidencias/docker/docker-01-imagen.png)

### Ejecutar los contenedores

Crear la red y levantar PostgreSQL. `POSTGRES_DB=logistock` crea la base de datos que la API necesita; el usuario y la contraseña coinciden con los valores por defecto de `application.yaml`.

```bash
docker network create practica2-net

docker run -d --name practica2-db --network practica2-net \
  -e POSTGRES_DB=logistock \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  postgres:16

docker exec practica2-db pg_isready -U postgres -d logistock
```

Cuando `pg_isready` responde `accepting connections`, levantar la API. `DB_HOST` apunta al nombre del contenedor de PostgreSQL, porque dentro del contenedor `localhost` sería la propia API.

```bash
docker run -d --name practica2-api --network practica2-net \
  -p 8080:8080 \
  -e DB_HOST=practica2-db \
  practica2-api:v1
```

![Comandos de red, PostgreSQL y API](docs/evidencias/docker/docker-02-comandos-run.png)

### Validar la ejecución

```bash
docker ps
docker logs practica2-api
```

En los logs debe aparecer `Started InventoryApplication` y `Tomcat started on port 8080`. En `docker ps` la API debe mostrar `0.0.0.0:8080->8080/tcp`.

![docker ps y docker logs](docs/evidencias/docker/docker-03-ps-logs.png)

![Contenedores en Docker Desktop](docs/evidencias/docker/docker-07-docker-desktop.png)

### Probar la API

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Especificación OpenAPI: `http://localhost:8080/v3/api-docs`
- Prueba rápida por terminal:

```bash
curl -i http://localhost:8080/api/v1/products
```

Debe responder `200 OK` con una página vacía la primera vez.

![Swagger UI](docs/evidencias/docker/docker-04-swagger-ui.png)

Desde Swagger UI se creó un producto con `POST /api/v1/products` (respuesta `201`) y luego se consultó con `GET /api/v1/products` (respuesta `200`), lo que confirma que la API escribe y lee de PostgreSQL dentro de Docker.

![POST /api/v1/products con respuesta 201](docs/evidencias/docker/docker-05-post-201.png)

![GET /api/v1/products con respuesta 200](docs/evidencias/docker/docker-06-get-200.png)

### Detener y limpiar

```bash
docker rm -f practica2-api practica2-db
docker network rm practica2-net
```

PostgreSQL no usa volumen, por lo que los datos se pierden al eliminar el contenedor `practica2-db`.

### Variables de entorno de la API

Son las mismas de la sección [PostgreSQL](#postgresql). En Docker solo es necesario definir `DB_HOST`; el resto usa los valores por defecto (`DB_PORT=5432`, `DB_NAME=logistock`, `DB_USERNAME=postgres`, `DB_PASSWORD=postgres`).

### Decisiones del Dockerfile

- **Multi-stage:** la primera etapa (`eclipse-temurin:21-jdk`) compila con Gradle Wrapper; la segunda (`eclipse-temurin:21-jre-alpine`) solo contiene el JRE y el jar. El JDK, Gradle y el código fuente no llegan a la imagen final.
- **Caché de capas:** primero se copian los archivos de Gradle y se descargan las dependencias, y después se copia `src`. Cambiar código no vuelve a descargar librerías.
- **`bootJar`:** genera un único jar ejecutable, lo que hace seguro el `COPY ... *.jar` (la tarea `assemble` también generaría un `-plain.jar`).
- **Usuario sin privilegios:** el proceso corre como el usuario `spring`, no como root.
- **`-XX:MaxRAMPercentage=75.0`:** hace que la JVM calcule el heap a partir del límite de memoria del contenedor.
- **`EXPOSE 8080`:** solo documenta el puerto; la publicación real se hace con `-p` en `docker run`.
- **`.dockerignore`:** excluye `.git`, `build/`, `.gradle` y archivos de IDE para mantener pequeño el contexto de build.

## Kubernetes

La API se despliega en el clúster local de Docker Desktop junto con PostgreSQL, dentro del namespace `practica2`. Los manifiestos están en la carpeta [`k8s/`](k8s/) y usan la misma imagen `practica2-api:v1` construida en la sección [Docker](#docker).

### Requisitos

- Docker Desktop con Kubernetes habilitado (*Settings → Kubernetes → Enable Kubernetes*, clúster tipo kubeadm)
- `kubectl` apuntando al contexto `docker-desktop`
- Imagen `practica2-api:v1` construida localmente (`docker build -t practica2-api:v1 .`)
- Puerto `30080` libre en la máquina local

### Manifiestos

| Archivo | Recursos | Descripción |
| --- | --- | --- |
| `k8s/00-namespace.yaml` | Namespace `practica2` | Aísla todos los recursos de la práctica |
| `k8s/01-postgres-secret.yaml` | Secret `postgres-secret` | Nombre de la base de datos, usuario y contraseña |
| `k8s/02-postgres.yaml` | Deployment `postgres` + Service ClusterIP `postgres-service` | PostgreSQL 16 accesible solo dentro del clúster |
| `k8s/03-api-configmap.yaml` | ConfigMap `api-config` | `DB_HOST`, `DB_PORT` y `DB_NAME` de la API |
| `k8s/04-api-deployment.yaml` | Deployment `logistock-api` | API con imagen `practica2-api:v1`, `imagePullPolicy: IfNotPresent`, requests/limits y probes |
| `k8s/05-api-service.yaml` | Service NodePort `logistock-api-service` | Expone la API en el puerto `30080` del equipo |

Todos los recursos comparten labels coherentes: los pods de la API llevan `app: logistock-api` y los de la base de datos `app: postgres`; los `selector` de cada Deployment y Service usan exactamente esos labels.

### Puertos

| Servicio | Tipo | Puerto del Service | Puerto del contenedor | Puerto en el equipo |
| --- | --- | --- | --- | --- |
| `logistock-api-service` | NodePort | `80` | `8080` | `30080` |
| `postgres-service` | ClusterIP | `5432` | `5432` | No se publica |

### Recursos asignados

| Contenedor | CPU request | CPU limit | Memoria request | Memoria limit |
| --- | --- | --- | --- | --- |
| `logistock-api` | `250m` | `1` | `512Mi` | `1Gi` |
| `postgres` | `100m` | `500m` | `256Mi` | `512Mi` |
| `wait-for-postgres` (init) | `50m` | `100m` | `32Mi` | `64Mi` |

Con `-XX:MaxRAMPercentage=75.0` (definido en el `Dockerfile`), la JVM limita su heap a cerca de 768 MB dentro del límite de `1Gi`.

### Desplegar

```bash
kubectl config use-context docker-desktop
docker pull postgres:16
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/
kubectl get pods -n practica2 -w
```

El pod de la API pasa por `Init:0/1` mientras el `initContainer` espera a que PostgreSQL acepte conexiones, luego por `Running 0/1` mientras Spring Boot arranca, y queda en `Running 1/1` cuando `/v3/api-docs` responde.

### Validar el despliegue

```bash
kubectl get pods -n practica2
kubectl get svc -n practica2
kubectl describe deployment logistock-api -n practica2
kubectl logs deployment/logistock-api -n practica2
```

![kubectl apply](docs/evidencias/kubernetes/kubernetes-01-manifest.png)

![kubectl get pods](docs/evidencias/kubernetes/k8s-02-get-pods.png)

![kubectl get svc](docs/evidencias/kubernetes/k8s-03-get-services.png)

![kubectl describe deployment](docs/evidencias/kubernetes/k8s-04-describe.png)

![kubectl logs](docs/evidencias/kubernetes/k8s-05-logs.png)

### Probar la API

- Swagger UI: `http://localhost:30080/swagger-ui.html`
- Especificación OpenAPI: `http://localhost:30080/v3/api-docs`

```bash
curl -i http://localhost:30080/api/v1/products
```

Si el NodePort no responde en el equipo, se puede usar port-forward y entrar por `http://localhost:8082`:

```bash
kubectl port-forward -n practica2 svc/logistock-api-service 8082:80
```

![Swagger UI en Kubernetes](docs/evidencias/kubernetes/running-kubernetes.png)

![POST GET /api/v1/products con respuesta 201](docs/evidencias/kubernetes/test_api_post_get.png)

![Manifiestos YAML](docs/evidencias/kubernetes/images_and_files_yaml.png)

### Eliminar el despliegue

```bash
kubectl delete namespace practica2
```

PostgreSQL usa un volumen `emptyDir`, por lo que los datos se pierden cuando el pod se elimina o se recrea.

### Decisiones de los manifiestos

- **Namespace `practica2`:** agrupa y aísla los recursos; borrarlo limpia todo el despliegue.
- **Secret + ConfigMap:** separan las credenciales de la configuración no sensible y evitan escribirlas en el Deployment.
- **`imagePullPolicy: IfNotPresent`:** la imagen `practica2-api:v1` existe solo en el Docker local; con esta política Kubernetes la usa sin intentar descargarla de un registro.
- **`initContainer` `wait-for-postgres`:** evita que la API falle al arrancar antes de que la base de datos esté lista.
- **Readiness y liveness probes:** el Service solo envía tráfico al pod cuando la API responde, y Kubernetes la reinicia si deja de aceptar conexiones.
- **ClusterIP para PostgreSQL y NodePort para la API:** la base de datos no queda expuesta fuera del clúster; solo la API es accesible desde el equipo.



## Validación con Postman

Las pruebas se hicieron con dos colecciones de Postman, una por entorno. Cada una usa un environment con la variable `base_url`.

| Entorno | Colección | `base_url` |
| --- | --- | --- |
| Docker | [Despliegue Docker](docs/evidencias/postman/docker/Despliegue_Docker.postman_collection.json) | `http://localhost:8080` |
| Kubernetes | [Despliegue Kubernetes](docs/evidencias/postman/kubernetes/Despliegue_Kubernetes.postman_collection.json) | `http://localhost:8082` (port-forward) |

Orden de ejecución: crear productos, registrar entradas y salidas, crear pedido, consultas y, al final, eliminar.

### Variables de Postman

Las colecciones usan variables para construir las rutas (`base_url`, `products`, `inventory`, `orders`). Estas son las variables definidas en cada entorno:

**Docker**
![Variables de Postman en Docker](docs/evidencias/postman/docker/Variables.png)

**Kubernetes**
![Variables de Postman en Kubernetes](docs/evidencias/postman/kubernetes/Variables.jpeg)

### Pruebas en Docker

#### Products
**Create product 2**
![Create product 2](docs/evidencias/postman/docker/create-product-2.png)

**Create product 3**
![Create product 3](docs/evidencias/postman/docker/create-product-3.png)

**List products**
![List products](docs/evidencias/postman/docker/list-products.png)

**Get product by id**
![Get product by id](docs/evidencias/postman/docker/get-product-by-id.png)

**List restock**
![List restock](docs/evidencias/postman/docker/list-restock.png)

**Delete product 1**
![Delete product 1](docs/evidencias/postman/docker/delete-product-1.jpeg)

**Delete product 2 (con verificación)**
![Delete product 2](docs/evidencias/postman/docker/delete-product-2.png)

#### Inventory
**Register entries**
![Register entries](docs/evidencias/postman/docker/register-entries.png)

**Register exits**
![Register exits](docs/evidencias/postman/docker/register-exits.png)

**List movements**
![List movements](docs/evidencias/postman/docker/list-movements.jpeg)

**List mov product**
![List mov product](docs/evidencias/postman/docker/list-mov-product.png)

#### Orders
**Create order**
![Create order](docs/evidencias/postman/docker/create-order.png)

**List orders**
![List orders](docs/evidencias/postman/docker/list-orders.png)

**Get orders by id**
![Get orders by id](docs/evidencias/postman/docker/get-orders-by-id.jpeg)

### Pruebas en Kubernetes

Se accedió por `kubectl port-forward` en el puerto `8082`, porque el NodePort `30080` no respondió en `localhost`.

#### Products
**Create product 1**
![Create product 1](docs/evidencias/postman/kubernetes/create-product-1.png)

**Create product 2**
![Create product 2](docs/evidencias/postman/kubernetes/create-product-2.png)

**Create product 3**
![Create product 3](docs/evidencias/postman/kubernetes/create-product-3.png)

**List products**
![List products](docs/evidencias/postman/kubernetes/list-products.png)

**Get product by id**
![Get product by id](docs/evidencias/postman/kubernetes/get-product-by-id.png)

**List restock**
![List restock](docs/evidencias/postman/kubernetes/list-restock.png)

**Delete product 1**
![Delete product 1](docs/evidencias/postman/kubernetes/delete-product-1.jpeg)

**Delete product 2 (con verificación)**
![Delete product 2](docs/evidencias/postman/kubernetes/delete-product-2.png)

#### Inventory
**Register entries**
![Register entries](docs/evidencias/postman/kubernetes/register-entries.png)

**Register exits**
![Register exits](docs/evidencias/postman/kubernetes/register-exits.jpeg)

**List movements**
![List movements](docs/evidencias/postman/kubernetes/list-movements.png)

**List mov product**
![List mov product](docs/evidencias/postman/kubernetes/list-mov-product.png)

#### Orders
**Create order**
![Create order](docs/evidencias/postman/kubernetes/create-order.png)

**List orders**
![List orders](docs/evidencias/postman/kubernetes/list-orders.png)

**Get orders by id**
![Get orders by id](docs/evidencias/postman/kubernetes/get-orders-by-id.png)



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

## Listar pedidos

```http
GET /api/v1/orders?page=0&size=10
```

Los pedidos se devuelven desde el más reciente hasta el más antiguo. Cuando dos pedidos tienen la misma fecha, el identificador descendente garantiza un orden estable. Cada elemento incluye todos los ítems del pedido.

Parámetros:

| Parámetro | Predeterminado | Descripción |
| --- | --- | --- |
| `page` | `0` | Número de página, comenzando en cero |
| `size` | `10` | Elementos por página, entre 1 y 100 |

Ejemplo con cURL:

```bash
curl --request GET \
  --url 'http://localhost:8080/api/v1/orders?page=0&size=10'
```

Respuesta `200 OK`:

```json
{
  "content": [
    {
      "id": 2,
      "createdAt": "2026-09-28T22:30:00Z",
      "status": "CREATED",
      "items": [
        {
          "id": 2,
          "productId": 1,
          "quantity": 2
        }
      ]
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

Si no existen pedidos se devuelve una página vacía. Una paginación inválida produce `400 Bad Request` con `INVALID_PAGINATION`.

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
- Listado paginado de pedidos con todos sus ítems.
- Orden descendente por fecha e identificador.
- Página vacía cuando no existen pedidos.
- Validación de la paginación de pedidos.
- Publicación del listado de pedidos en OpenAPI.
- Inicio del contexto de Spring.

Durante las pruebas se utiliza una base H2 en memoria con compatibilidad PostgreSQL, base lógica `logistock` y esquema `inventory`.

## Flujo de desarrollo

Cada endpoint se desarrolla en una rama `feature/*` independiente. Antes de realizar commits o push, los cambios deben ser revisados manualmente.

La feature actual se encuentra en:

```text
feature/list-orders
```

## Checklist de entrega

- [x] La API funciona dentro de Docker.
- [x] La imagen tiene una etiqueta de versión (`v1`).
- [x] Kubernetes tiene pods en estado Running.
- [x] El Service permite acceder a la API.
- [x] Namespace `practica2` configurado.
- [x] Requests y limits definidos.
- [ ] Repositorio compartido con `oalarconpe`.
- [ ] README completo (integrantes y video).
- [ ] Video publicado en YouTube y enlazado.
- [x] Evidencias visuales incluidas.
- [ ] Reflexión técnica (máximo una página).
