# Entrega Continua (CD) – Azure DevOps + Azure App Service

**Responsable:** Sebastián Restrepo Mira
**Pipeline:** [`pipelines/cd-azure-pipelines.yml`](../pipelines/cd-azure-pipelines.yml)
**Evidencias:** [`evidencias/cd/`](../evidencias/cd/)

## 1. Flujo

```
commit en main ──► logistock-ci (build + test + artefacto logistock-jar)
                        │ termina con éxito
                        ▼
                  logistock-cd ──► descarga logistock-jar ──► AzureWebApp@1 ──► App Service
                                                                                  │
                                                         smoke test GET /health ◄─┘
```

- La CD **no tiene trigger por commits** (`trigger: none`). Se activa mediante un
  *pipeline resource trigger*: cuando `logistock-ci` termina bien sobre `main`.
- La CD **no recompila**. Descarga exactamente el artefacto que publicó esa
  ejecución de CI (`download: ci`).
- El primer paso imprime la ejecución de CI y el commit de origen
  (`resources.pipeline.ci.runName` / `sourceCommit`). Con eso se relaciona
  commit → CI → CD.

## 2. Recurso Azure App Service (configuración esencial)

| Elemento | Valor |
|---|---|
| Nombre | `logistock-api-srm` |
| URL pública | https://logistock-api-srm-agabdzh4c0bdfgg2.chilecentral-01.azurewebsites.net |
| Región | Chile Central |
| Pila | Java 21 (Java SE, servidor integrado de Spring Boot) |
| Sistema operativo | Linux (por eso la tarea usa `appType: webAppLinux`) |
| Grupo de recursos | `rg-logistock` |
| HTTPS Only | Activado |
| Comprobación de salud | `/health` |
| Base de datos | Azure Database for PostgreSQL |

**Variables de entorno (App Settings), sin valores en el repo:**
`SPRING_PROFILES_ACTIVE`, `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`,
`DB_PASSWORD`, `SERVER_PORT`, `WEBSITE_HEALTHCHECK_MAXPINGFAILURES`.
Los valores se ven ocultos en el portal ("Mostrar valor") y no existen en el
repositorio; `application.yaml` solo referencia los nombres `${DB_...}`.

## 3. Configuración en Azure DevOps (paso a paso)

1. **Paralelismo:** Organization settings → Parallel jobs. Si "Microsoft-hosted"
   muestra 0, solicitar el grant gratuito (formulario *Azure DevOps Parallelism
   Request*). Mientras tanto se puede usar un agente propio (*self-hosted*) en el
   pool `Default` (ver comentario en el YAML).
2. **Conexión de servicio:** Project settings → Service connections → New →
   *Azure Resource Manager* → *Workload Identity federation (automatic)* →
   suscripción y grupo de recursos del App Service → nombre
   **`sc-azure-logistock`**. No se marca "Grant access to all pipelines"; se
   autoriza solo para la CD la primera vez que corre.
3. **Environment:** Pipelines → Environments → New → `logistock-produccion`
   (Resource: None). Aquí se ve el historial de despliegues. Opcional: agregar un
   *Approval* (ver sección 5).
4. **Pipeline:** Pipelines → New pipeline → GitHub → repo `logistock-api` →
   *Existing Azure Pipelines YAML file* → `/pipelines/cd-azure-pipelines.yml` →
   Save. Renombrarla a **`logistock-cd`**.
5. **Prueba de punta a punta:** hacer un commit en `main` y comprobar que corre
   `logistock-ci` y, al terminar, `logistock-cd` sola.

## 4. Medidas contra exposición de secretos

- Credenciales de PostgreSQL solo en App Settings del App Service.
- La autenticación DevOps → Azure usa *Workload Identity federation*: no hay
  contraseña ni *client secret* guardados en ningún lado.
- El YAML solo contiene nombres de recursos (no son secretos).
- `.gitignore` excluye artefactos compilados (`*.jar`, `build/`).

## 5. Aprobación (opcional)

Si se agrega un *Approval* al environment `logistock-produccion`, la CD queda
en pausa hasta que alguien la apruebe. **Efecto en la automatización:** el
despliegue sigue siendo el mismo artefacto y la misma pipeline, pero exige una
acción humana. Para la demo se recomienda **sin aprobación**, para mostrar el
flujo 100 % automático.

## 6. Problemas encontrados y solución

| # | Problema | Causa | Solución |
|---|---|---|---|
| 1 | La CD terminaba en verde pero `/health` seguía mostrando la versión anterior | El `.jar` se subía con su nombre original (`inventory-0.0.1-SNAPSHOT.jar`); App Service en Linux ejecuta `app.jar`, así que seguía corriendo el de la práctica 3. El smoke test solo comprobaba `"status":"UP"`, que la versión vieja también cumple | Se agregó el paso "Preparar paquete app.jar" y el smoke test ahora exige que `/health` reporte el mismo commit que el artefacto de CI |
| 2 | La primera ejecución de CD quedó en pausa con el aviso *"This pipeline needs permission to access 2 resources"* | La conexión `sc-azure-logistock` y el environment `logistock-produccion` se crearon sin *Grant access to all pipelines*, a propósito | Se autorizaron (*Permit*) solo para `logistock-cd`. Las demás pipelines del proyecto no pueden usar la credencial de Azure |
| 3 | Al lanzar la CD manualmente, Azure DevOps proponía por defecto el artefacto de una ejecución de CI de un Pull Request (`pull/23/merge`) | En una ejecución manual se ofrece la última ejecución de CI, sea de la rama que sea | Se eligió a mano la ejecución de `main`. En las ejecuciones automáticas no ocurre, porque el recurso `ci` solo dispara con `branches: include: main` |
| 4 | Se esperaba que la organización nueva de Azure DevOps no tuviera agente hospedado gratuito | Política de Microsoft para organizaciones nuevas | No fue necesario solicitarlo: al vincular la suscripción Azure for Students quedó 1 parallel job con 1800 min/mes |
