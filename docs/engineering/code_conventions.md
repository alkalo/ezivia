# Convenciones de código de Ezivia

Ezivia está pensada para ofrecer una experiencia simplificada para personas mayores. Estas
convenciones aseguran que el código refleje esa intención.

## Lenguaje y estilo
- Todo el código nuevo debe escribirse en **Kotlin**.
- Sigue el estilo oficial de Kotlin (`kotlin.code.style=official`). Usa `ktlint` o el formateador
  de Android Studio antes de abrir un PR.
- Prefiere clases y funciones pequeñas y descriptivas. Los nombres deben comunicar el propósito
  para la audiencia senior (por ejemplo `OnboardingNavigator`, `DeviceProfileProvider`).

## Android
- El `minSdk` del proyecto es **26 (Android 8.0)**. No introducir APIs que rompan esta
  compatibilidad sin un plan de compatibilidad.
- Habilita `viewBinding` en pantallas nuevas para evitar `findViewById`.
- Organiza las responsabilidades por módulo:
  - `launcher`: aplicación y flujo principal.
  - `communication`: utilidades de mensajería y contacto.
  - `utilities`: helpers compartidos, persistencia ligera.
  - `onboarding`: flujo inicial para configurar Ezivia.
- Todas las cadenas visibles para personas usuarias deben vivir en `strings.xml` y estar en
  español neutro.

## Accesibilidad
- Mantén tamaños de fuente mínimos de `16sp` en layouts y contrasta colores con un ratio mínimo
  AA.
- Cuando agregues nuevas pantallas, valida la navegación con lector de pantalla.

## Documentación y pruebas
- Actualiza esta carpeta con contexto cuando agregues herramientas de desarrollo nuevas.
- Incluye instrucciones de prueba manual para características visibles.
