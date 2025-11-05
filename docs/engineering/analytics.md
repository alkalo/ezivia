# Configuración de analítica y crash reporting

La app **ezivia** prioriza la sencillez y la privacidad de las personas mayores. Para
monitorizar el uso y anticipar fallos críticos optamos por el ecosistema de Firebase:

- **Firebase Analytics** para medir eventos clave de adopción y soporte.
- **Firebase Crashlytics** para recibir informes de fallos en tiempo real.

Ambos servicios están bien soportados en Android, ofrecen SDKs ligeros y permiten aplicar
políticas de anonimización antes de transmitir datos sensibles.

## Preparación del proyecto

1. **Crear el proyecto en Firebase Console.**
   - Registrar la aplicación Android (package `com.ezivia.launcher`).
   - Descargar el archivo `google-services.json` y colocarlo en `app/launcher/src/main/`.
2. **Añadir el repositorio de Google Services** en `app/build.gradle.kts` si aún no existe:
   ```kotlin
   buildscript {
       dependencies {
           classpath("com.google.gms:google-services:4.4.1")
           classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
       }
   }
   ```
3. **Aplicar plugins en el módulo `:launcher`:**
   ```kotlin
   plugins {
       id("com.google.gms.google-services")
       id("com.google.firebase.crashlytics")
   }
   ```
4. **Agregar dependencias de los nuevos módulos** (ya definidos en `app/settings.gradle.kts`):
   ```kotlin
   dependencies {
       implementation(project(":analytics"))
       implementation(project(":logging"))
   }
   ```
5. **Sincronizar Gradle** y verificar que las dependencias
   `com.google.firebase:firebase-analytics-ktx` y
   `com.google.firebase:firebase-crashlytics-ktx` se resuelvan correctamente.

## Uso de los módulos `:analytics` y `:logging`

- `AnalyticsManager` centraliza el envío de eventos. Registra únicamente datos anonimizados
  usando `DataAnonymizer`.
- `FirebaseAnalyticsTracker` traduce cada evento a un `Bundle` y lo envía al SDK.
- `CrashReportingLogger` procesa los errores, los anonimiza con `LogAnonymizer` y los remite a
  `FirebaseCrashlyticsReporter`.

Se recomienda inicializar ambos módulos en la `Application` de producción e inyectar
implementaciones `NoOp` durante pruebas locales.

## Modelado de eventos

Los eventos definidos en `AnalyticsEvent` cubren los hitos principales del producto:

| Evento                     | Descripción                                            | Claves sensibles |
|----------------------------|--------------------------------------------------------|------------------|
| `AppOpened`                | Inicio de la app por parte de la persona usuaria.     | Ninguna          |
| `FeatureAccessed`          | Uso de funciones simplificadas (contactos, linterna). | Ninguna          |
| `ContactDialed`            | Llamadas rápidas a contactos frecuentes.               | `contact_name`   |
| `EmergencyTriggered`       | Activación/cancelación de alertas SOS.                | Ninguna          |
| `SupportRequested`         | Visita al flujo de ayuda.                             | Ninguna          |
| `AccessibilityAdjusted`    | Cambios de tamaño de texto o contraste.               | Ninguna          |

Las claves marcadas como sensibles se hashéan automáticamente antes de salir del dispositivo.

## Buenas prácticas de privacidad

- Evitar enviar números de teléfono, direcciones exactas u otros identificadores directos.
- Usar el método `sanitize` de los eventos siempre que se trabaje con PII.
- Configurar en Firebase la retención de datos y la desactivación de atributos publicitarios.

## Validaciones recomendadas

- Ejecutar pruebas manuales abriendo la app, provocando un fallo controlado y verificando que
  aparece en Crashlytics con los atributos anonimizados.
- Revisar el panel de DebugView para confirmar que los eventos se reciben correctamente y sin
  información personal en claro.
