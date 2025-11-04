# Integración continua

Este repositorio usa GitHub Actions para validar la aplicación Android.

## Flujo actual
- Ejecuta las tareas `lint` y `test` dentro del proyecto `app/` mediante `gradle/gradle-build-action@v2`.
- Configura Java 17 y descarga las herramientas más recientes del SDK de Android.

## Próximos pasos sugeridos
- Añadir análisis estático (por ejemplo, `detekt`).
- Publicar artefactos de `apk` para pruebas internas.
