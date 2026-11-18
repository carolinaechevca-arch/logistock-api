# Integracion Continua (CI) - Azure DevOps

**Responsable:** Ferney Lopez Copete
**Pipeline:** [`pipelines/ci-azure-pipelines.yml`](../pipelines/ci-azure-pipelines.yml)
**Nombre en Azure DevOps:** `logistock-ci`
**Evidencias:** [`evidencias/ci/`](../evidencias/ci/)

## 1. Que hace

- Se dispara sola con cada commit en `main` (trigger CI) y valida los Pull Requests hacia `main`.
- No se dispara si el commit solo cambia `README.md`, `docs/` o `evidencias/`.
- Agente `ubuntu-latest` (Microsoft-hosted, free tier) con JDK 21.
- Pasos: restaurar dependencias, compilar, ejecutar pruebas (JUnit 5 + H2), publicar resultados, `bootJar`, validar que haya un solo jar y publicar el artefacto `logistock-jar`.
- La CD (`logistock-cd`) descarga ese artefacto; no se recompila.

## 2. Pruebas automatizadas

<total> pruebas en <n> clases (dominio, casos de uso y controladores). Usan H2 en memoria en modo PostgreSQL, por eso la CI no necesita base de datos ni secretos.

## 3. Ultima ejecucion verificada

| Elemento | Valor |
|---|---|
| Ejecucion | CI-20261118.4 - https://dev.azure.com/logistock-team/logistock/_build |
| Motivo | Individual CI |
| Commit | main - e326b2d3 |
| Pruebas | 95 aprobadas, 0 fallidas |
| Artefacto | logistock-jar -> inventory-0.0.1-SNAPSHOT.jar |

