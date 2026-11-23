# Evidencias – Gestión de GitHub

Responsable: Diana Carolina Echeverri Cadavid (propietaria del repositorio
`carolinaechevca-arch/logistock-api`). Capturas tomadas por ella y subidas al
repositorio por Sebastián Restrepo Mira por problemas con su equipo.

| Archivo | Qué se ve |
|---|---|
| `01-github-app-azure-pipelines.png` | Instalación de la GitHub App **Azure Pipelines** en la cuenta propietaria del repositorio, limitada a **un solo repositorio** (`logistock-api`, opción *Only select repositories*) y con los permisos que necesita para leer el código y publicar el estado de las ejecuciones en commits y Pull Requests. |
| `02-devops-seleccion-repositorio.png` | En Azure DevOps (`logistock-team / logistock`), el repositorio `carolinaechevca-arch/logistock-api` ya aparece disponible para crear pipelines después de autorizar la app. |

Decisión de seguridad: se eligió *Only select repositories* en lugar de
*All repositories*, para que Azure Pipelines tenga acceso únicamente al
repositorio del proyecto (principio de mínimo privilegio).
