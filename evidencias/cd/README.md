# Evidencias – Entrega Continua (CD)

Responsable: Sebastián Restrepo Mira.

Cambio rastreado de punta a punta: commit `8f448f0` en `main` →
`logistock-ci` **CI-20261120.3** → `logistock-cd` **#20261120.2** (disparada
automáticamente) → `/health` en Azure con `ciBuild: CI-20261120.3` y
`commit: 8f448f0…`.

| Archivo | Qué se ve |
|---|---|
| `00-devops-usuarios.png` | Los 5 integrantes con acceso Basic a la organización `logistock-team` |
| `01-devops-parallel-jobs.png` | Agente hospedado gratuito (1 parallel job, 1800 min/mes) |
| `02-service-connection.png` | Conexión `sc-azure-logistock` (Azure Resource Manager, Workload Identity federation) |
| `03-environment.png` | Environment `logistock-produccion` |
| `04-cd-pipeline-yaml.png` | `pipelines/cd-azure-pipelines.yml` al crear la pipeline en Azure DevOps |
| `04b-cd-primera-ejecucion-manual.png` | Primera ejecución (manual) para autorizar permisos y validar el despliegue |
| `05-cd-trigger-desde-ci.png` | CD "Automatically triggered by ci/CI-20261120.3", rama `main`, commit `8f448f0` |
| `06-cd-trazabilidad.png` | Log "Trazabilidad": ejecución de CI, rama, commit y artefacto |
| `07a-cd-artefacto-recibido.png` | Log "Mostrar artefacto recibido": el `.jar` publicado por la CI |
| `07b-cd-artefacto-recibido.png` | Log "Preparar paquete app.jar" |
| `08-cd-deploy-exitoso.png` | Log "Desplegar .jar en App Service": ZIP Deploy exitoso |
| `09-cd-smoke-test.png` | Smoke test: los primeros intentos responden la versión anterior; el 4.º responde el commit esperado |
| `10-environment-historial.png` | Environment con el historial de despliegues |
| `11-azure-deployment-center.png` | App Service → Centro de implementación → Registros |
| `12-azure-app-settings.png` | Variables de entorno del App Service (valores ocultos) |
| `13-url-publica-cambio.png` | `/health` en el navegador con `version`, `ciBuild` y `commit` |
| `14a-github-commit.png` | Commit `8f448f0` en `main` en GitHub |
| `14b-ci-mismo-commit.png` | `logistock-ci` CI-20261120.3 sobre el commit `8f448f0` con el artefacto publicado |

La relación CD ↔ commit también se ve en `05-cd-trigger-desde-ci.png`.
