# Informe de pruebas de accesibilidad manual

**Fecha:** 2024-05-14
**Revisión realizada por:** Automatización CI (entorno sin dispositivo)

## Alcance
- Actividad principal `HomeActivity` (navegación y contactos favoritos).
- Flujos de protección con PIN (`ProtectionManager`).
- Ajustes restringidos (`RestrictedSettingsActivity`).

## Método
Debido a la falta de emulador o dispositivo accesible en el entorno remoto, no se pudieron ejecutar interacciones manuales reales. Se realizó una inspección heurística del código fuente y de los layouts disponibles para identificar riesgos de accesibilidad.

## Hallazgos
1. **Contraste y legibilidad no verificados:** No es posible confirmar el contraste real de botones primarios ni del texto de contactos sin capturas de pantalla.
2. **Acciones críticas protegidas por diálogo PIN:** El diálogo utiliza `TextInputLayout` (`DialogPinPromptBinding`), lo cual debería exponer etiquetas accesibles, pero se recomienda verificarlo en dispositivo con lector de pantalla.
3. **Mensajes de error:** El mensaje de PIN incorrecto se establece mediante `setError`, lo que es compatible con TalkBack, pero debe validarse auditivamente.
4. **Listas y botones:** `FavoriteContactsAdapter` asigna manejadores de click a llamadas y mensajes; se debe revisar que los `contentDescription` de los íconos (si existen en el layout) estén localizados.

## Bloqueadores
- Sin acceso a emulador o build instrumentado, no se pudieron validar gestos, navegación con lector de pantalla ni tamaño de toque mínimo.

## Recomendaciones
- Ejecutar pruebas con TalkBack en un dispositivo real, siguiendo la guía de accesibilidad de Android.
- Validar contraste con herramientas como Accessibility Scanner.
- Documentar capturas para posteriores revisiones.

## Próximos pasos sugeridos
1. Generar build debug e instalar en dispositivo físico.
2. Capturar incidencias con vídeos o capturas y subirlas a la carpeta de QA.
3. Re-ejecutar checklist WCAG 2.1 nivel AA específico para móviles.
