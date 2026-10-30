
# Plan de validación funcional – Azure App Service

## Proyecto
Logistock API – Práctica 3: Despliegue de Software

**Responsable:** Leidy Melissa Trejos Pamplona  
**Entorno:** Microsoft Azure App Service  
**Tecnología:** Java 21, Spring Boot y PostgreSQL

## 1. Objetivo

Verificar el funcionamiento de los servicios REST de Logistock API publicados en Microsoft Azure, mediante pruebas funcionales realizadas con Postman.

## 2. Alcance

La validación comprende tres módulos principales:

- Gestión de productos.
- Movimientos de inventario.
- Gestión de pedidos.

Las pruebas se ejecutarán contra la URL pública de Azure App Service.

## 3. Entorno de pruebas

**URL base:**

https://logistock-api-srm-agabdzh4c0bdfgg2.chilecentral-01.azurewebsites.net

**Herramienta:** Postman

**Endpoint de comprobación:** GET /health

## 4. Casos de prueba

| ID | Módulo | Método | Endpoint | Resultado esperado |
|---|---|---|---|---|
| CP-01 | Salud | GET | /health | HTTP 200 y estado UP |
| CP-02 | Productos | POST | /api/v1/products | Producto creado correctamente |
| CP-03 | Productos | GET | /api/v1/products | Listado de productos |
| CP-04 | Productos | GET | /api/v1/products/{id} | Información del producto |
| CP-05 | Inventario | POST | /api/v1/inventory/entries | Entrada registrada |
| CP-06 | Inventario | POST | /api/v1/inventory/exits | Salida registrada |
| CP-07 | Inventario | GET | /api/v1/inventory/movements | Movimientos consultados |
| CP-08 | Pedidos | POST | /api/v1/orders | Pedido creado |
| CP-09 | Pedidos | GET | /api/v1/orders | Listado de pedidos |
| CP-10 | Pedidos | GET | /api/v1/orders/{id} | Pedido consultado |

## 5. Criterios de aceptación

- La API debe ser accesible mediante HTTPS.
- Los servicios deben devolver respuestas HTTP apropiadas.
- Los datos enviados y recibidos deben ser coherentes.
- Las operaciones de inventario deben reflejar los cambios realizados.
- Los pedidos deben poder crearse y consultarse.
- Las pruebas deben contar con evidencias verificables.

## 6. Evidencias

Las capturas y la colección de Postman se almacenarán en:

evidencias/azure/postman/

## 7. Estado de ejecución

Pendiente de ejecución de las pruebas funcionales en Azure.

Los resultados se documentarán después de realizar las validaciones.
