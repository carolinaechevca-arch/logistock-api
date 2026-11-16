# Evidencias – Entrega Continua (CD)

Responsable: Sebastián Restrepo Mira. Guardar las capturas en esta carpeta con estos nombres.

| Archivo | Qué debe verse |
|---|---|
| `01-devops-parallel-jobs.png` | Organization settings → Parallel jobs (o el agente propio en línea) |
| `02-service-connection.png` | Conexión `sc-azure-logistock` creada (tipo Azure Resource Manager) |
| `03-environment.png` | Environment `logistock-produccion` |
| `04-cd-pipeline-yaml.png` | `cd-azure-pipelines.yml` abierto en Azure DevOps |
| `05-cd-trigger-desde-ci.png` | Ejecución de CD con "Triggered by logistock-ci" |
| `06-cd-trazabilidad.png` | Log del paso "Trazabilidad": ejecución de CI y commit |
| `07-cd-artefacto-recibido.png` | Log "Mostrar artefacto recibido" con el `.jar` |
| `08-cd-deploy-exitoso.png` | Paso "Desplegar .jar en App Service" en verde |
| `09-cd-smoke-test.png` | Log del smoke test con `"status":"UP"` |
| `10-environment-historial.png` | Environment con el historial de despliegues |
| `11-azure-deployment-center.png` | App Service → Deployment Center → Logs con el despliegue |
| `12-azure-app-settings.png` | Variables de entorno (valores ocultos) |
| `13-url-publica-cambio.png` | `/health` en el navegador mostrando el cambio nuevo |
| `14-mismo-commit.png` | Mismo SHA en GitHub, CI y CD (captura combinada) |
